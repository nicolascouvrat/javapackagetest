package com.nikodoko.packagetest.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Repository;
import com.nikodoko.packagetest.internal.bazel.BuildFile;
import com.nikodoko.packagetest.internal.bazel.ModuleFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BazelExporter implements Exporter {
  private static final String NAME = "BAZEL_EXPORTER";
  private static final String MAIN_DIRECTORY = "src/main/java";
  private static final String TEST_DIRECTORY = "src/test/java";
  private static final Pattern TEST_FILE_RE = Pattern.compile(".+Test\\.java");

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Exported export(Path root, List<Repository> repositories, List<Module> modules)
      throws IOException {
    ExportedBuilder to = new ExportedBuilder().root(root);
    writeModuleFile(to, repositories, modules);
    for (Module m : modules) {
      exportModule(m, to);
    }

    return to.build();
  }

  private void writeModuleFile(
      ExportedBuilder to, List<Repository> repositories, List<Module> modules) throws IOException {
    Set<Module.Dependency> deps = gatherDependencies(modules);
    Path target = to.root().resolve("MODULE.bazel");
    ModuleFile mf =
        ModuleFile.builder()
            .artifacts(deps.stream().map(BazelExporter::toModuleDep).toList())
            .repositories(repositories.stream().map(BazelExporter::toModuleRepo).toList())
            .build();
    mf.write(target);
    to.markAsWritten("", "MODULE.bazel", target);
  }

  private Set<Module.Dependency> gatherDependencies(List<Module> modules) {
    return modules.stream()
        .flatMap(m -> StreamSupport.stream(m.dependencies().spliterator(), false))
        .collect(Collectors.toSet());
  }

  private void exportModule(Module module, ExportedBuilder to) throws IOException {
    writeBuildFile(module, to);
    for (Module.File f : module.files()) {
      Path fullpath = filename(to.root(), module.name(), f.fragment());
      Files.createDirectories(fullpath.getParent());
      Files.write(fullpath, f.contents().getBytes(UTF_8));
      to.markAsWritten(module.name(), f.fragment(), fullpath);
    }
  }

  private void writeBuildFile(Module module, ExportedBuilder to) throws IOException {
    Path target = to.root().resolve(Paths.get(moduleName(module.name()), "BUILD.bazel"));
    Files.createDirectories(target.getParent());
    BuildFile bf =
        BuildFile.builder()
            .targetName(module.name())
            .srcs(buildSrcGlob())
            .srcsGlob()
            .deps(
                StreamSupport.stream(module.dependencies().spliterator(), false)
                    .map(BazelExporter::toBuildDep)
                    .toList())
            .build();

    bf.write(target);
    to.markAsWritten(module.name(), "BUILD.bazel", target);
  }

  private Path filename(Path root, String module, String fragment) {
    return root.resolve(relativePath(module, fragment));
  }

  // The usual maven multi-module architecture is
  // root
  //  |
  //  - pom.xml
  //  - module-name
  //    |
  //    - pom.xml
  //    - src/main/java/your/custom/path/Code.java
  // That convention is also used in bazel, but without pom files of course
  private Path relativePath(String module, String fragment) {
    String directory = MAIN_DIRECTORY;
    Matcher m = TEST_FILE_RE.matcher(fragment);
    if (m.matches()) {
      directory = TEST_DIRECTORY;
    }

    return Paths.get(moduleName(module), directory, module.replace(".", "/"), fragment);
  }

  private String moduleName(String module) {
    return module.replace(".", "");
  }

  private static String buildSrcGlob() {
    return String.format("%s/**/*.java", MAIN_DIRECTORY);
  }

  private static String toBuildDep(Module.Dependency d) {
    return String.format(
        "@maven//:%s_%s",
        d.groupId().replace('-', '_').replace('.', '_'),
        d.artifactId().replace('-', '_').replace('.', '_'));
  }

  private static String toModuleDep(Module.Dependency d) {
    return String.format("%s:%s:%s", d.groupId(), d.artifactId(), d.version());
  }

  private static String toModuleRepo(Repository r) {
    return r.url();
  }
}
