package com.nikodoko.packagetest.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Repository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;

class MavenExporter implements Exporter {
  private static final String NAME = "MAVEN_EXPORTER";
  private static final Pattern TEST_FILE_RE = Pattern.compile(".+Test\\.java");
  private static final String MAIN_DIRECTORY = "src/main/java";
  private static final String TEST_DIRECTORY = "src/test/java";
  private static final String PROJECT_GROUP_ID = "packagetest.maven";
  private static final String PROJECT_MODEL_VERSION = "4.0.0";
  private static final String PROJECT_VERSION = "1.0.0";
  private static final String VERSION_PROPERTY_TEMPLATE = "%s.version";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Exported export(Path root, List<Repository> repositories, List<Module> modules)
      throws IOException {
    ExportedBuilder to = new ExportedBuilder().root(root);
    for (Module m : modules) {
      exportModule(m, to);
    }

    return to.build();
  }

  private void exportModule(Module module, ExportedBuilder to) throws IOException {
    writePom(module, to, minimalPom(module));
    for (Module.File f : module.files()) {
      Path fullpath = filename(to.root(), module.name(), f.fragment());
      Files.createDirectories(fullpath.getParent());
      Files.write(fullpath, f.contents().getBytes(UTF_8));
      to.markAsWritten(module.name(), f.fragment(), fullpath);
    }
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
  // This is maven-specific and the general Exporter parameters do not include an entry for said
  // module name, so generate one from the module name (supposed to be your.custom.path in the
  // previous example).
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

  private Model minimalPom(Module module) {
    Model m = new Model();
    m.setModelVersion(PROJECT_MODEL_VERSION);
    m.setGroupId(PROJECT_GROUP_ID);
    m.setVersion(PROJECT_VERSION);
    m.setArtifactId(artifactId(module));
    m.setDependencies(dependencies(module));
    m.setDependencyManagement(dependencyManagement(module));
    m.setProperties(properties(module));
    return m;
  }

  private String artifactId(Module module) {
    return module.name().replace(".", "-");
  }

  private List<Dependency> dependencies(Module module) {
    if (module.moduleDependencies().iterator().hasNext()) {
      throw new IllegalArgumentException(NAME + " does not support module dependencies");
    }

    List<Dependency> dependencies = new ArrayList<>();
    for (Module.Dependency moduleDependency : module.dependencies()) {
      Dependency dependency = new Dependency();
      dependency.setGroupId(moduleDependency.groupId());
      dependency.setArtifactId(moduleDependency.artifactId());
      dependencies.add(dependency);
    }

    return dependencies;
  }

  private DependencyManagement dependencyManagement(Module module) {
    DependencyManagement dependencyManagement = new DependencyManagement();
    List<Dependency> dependencies = new ArrayList<>();
    for (Module.Dependency moduleDependency : module.dependencies()) {
      if (moduleDependency.version().equals("")) {
        continue;
      }

      Dependency dependency = new Dependency();
      dependency.setGroupId(moduleDependency.groupId());
      dependency.setArtifactId(moduleDependency.artifactId());
      dependency.setVersion(
          String.format(
              "${%s}", String.format(VERSION_PROPERTY_TEMPLATE, moduleDependency.artifactId())));
      dependencies.add(dependency);
    }

    dependencyManagement.setDependencies(dependencies);
    return dependencyManagement;
  }

  private Properties properties(Module module) {
    Properties props = new Properties();
    for (Module.Dependency moduleDependency : module.dependencies()) {
      if (moduleDependency.version().equals("")) {
        continue;
      }

      props.setProperty(
          String.format(VERSION_PROPERTY_TEMPLATE, moduleDependency.artifactId()),
          moduleDependency.version());
    }

    return props;
  }

  private void writePom(Module module, ExportedBuilder to, Model pom) throws IOException {
    ModelWriter writer = new DefaultModelWriter();
    Path target = to.root().resolve(Paths.get(moduleName(module.name()), "pom.xml"));
    writer.write(target.toFile(), null, pom);
    to.markAsWritten(module.name(), "pom.xml", target);
  }
}
