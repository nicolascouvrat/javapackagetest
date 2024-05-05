package com.nikodoko.packagetest.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
  public Exported export(Path root, Module... modules) throws IOException {
    ExportedBuilder to = new ExportedBuilder().root(root);
    writeRootModule(to, modules);
    for (Module m : modules) {
      exportModule(m, to);
    }

    return to.build();
  }

  private void writeRootModule(ExportedBuilder to, Module... modules) throws IOException {
    Set<Module.Dependency> deps = gatherDependencies(modules);
    Path target = to.root().resolve("MODULE.bazel");
    BazelFileWriter.writeModule(target, deps);
    to.markAsWritten("", "MODULE.bazel", target);
  }

  private Set<Module.Dependency> gatherDependencies(Module... modules) {
    return Arrays.stream(modules)
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
    BazelFileWriter.writeBuild(target, module, MAIN_DIRECTORY);
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
}
