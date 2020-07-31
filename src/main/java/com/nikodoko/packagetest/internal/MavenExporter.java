package com.nikodoko.packagetest.internal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelWriter;
import org.apache.maven.model.io.ModelWriter;

class MavenExporter implements Exporter {
  private static final String NAME = "MAVEN_EXPORTER";
  private static final Pattern TEST_FILE_RE = Pattern.compile(".+Test\\.java");
  private static final String MAIN_DIRECTORY = "src/main/java";
  private static final String TEST_DIRECTORY = "src/test/java";
  private static final String PROJECT_ROOT_ARTIFACT_ID = "packagetest-maven";
  private static final String PROJECT_GROUP_ID = "packagetest.maven";
  private static final String PROJECT_MODEL_VERSION = "4.0.0";
  private static final String PROJECT_VERSION = "1.0.0";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Exported export(Path root, Module... modules) throws IOException {
    Exported to = new Exported(root);
    for (Module m : modules) {
      exportModule(m, to);
    }

    return to;
  }

  private void exportModule(Module module, Exported to) throws IOException {
    writePom(module, to, minimalPom(artifactName(module.name())));
    for (Module.File f : module.files()) {
      Path fullpath = filename(to.root(), module.name(), f.fragment());
      Files.createDirectories(fullpath.getParent());
      Files.write(fullpath, f.contents().getBytes(UTF_8));
      to.markAsWritten(module.name(), f.fragment(), fullpath);
    }
  }

  private Path filename(Path root, String module, String fragment) {
    checkNotNull(root, "root path should not be null");
    checkNotNull(module, "module name should not be null");
    checkNotNull(fragment, "path fragment should not be null");
    checkArgument(!module.equals(""), "module name should not be empty");
    checkArgument(!fragment.equals(""), "path fragment should not be empty");

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

  private String artifactName(String module) {
    return module.replace(".", "-");
  }

  private Model minimalPom(String artifactId) {
    Model m = new Model();
    m.setModelVersion(PROJECT_MODEL_VERSION);
    m.setGroupId(PROJECT_GROUP_ID);
    m.setVersion(PROJECT_VERSION);
    m.setArtifactId(artifactId);
    return m;
  }

  private void writePom(Module module, Exported to, Model pom) throws IOException {
    ModelWriter writer = new DefaultModelWriter();
    Path target = to.root().resolve(Paths.get(moduleName(module.name()), "pom.xml"));
    writer.write(target.toFile(), null, pom);
    to.markAsWritten(module.name(), "pom.xml", target);
  }
}
