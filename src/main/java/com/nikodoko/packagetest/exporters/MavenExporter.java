package com.nikodoko.packagetest.exporters;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class MavenExporter implements Exporter {
  private static final String NAME = "MAVEN_EXPORTER";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public Exported export(List<Module> modules, Path root) throws IOException {
    Exported to = new Exported(root);
    for (Module m : modules) {
      exportModule(m, to);
    }

    return to;
  }

  private void exportModule(Module module, Exported to) throws IOException {
    for (Module.File f : module.files()) {
      Path fullpath = filename(to.root(), module.name(), f.fragment());
      Files.createDirectories(fullpath.getParent());
      Files.write(fullpath, f.content().getBytes(UTF_8));
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
    return Paths.get(moduleName(module), "src/main/java", module.replace(".", "/"), fragment);
  }

  private String moduleName(String module) {
    return module.replace(".", "");
  }
}
