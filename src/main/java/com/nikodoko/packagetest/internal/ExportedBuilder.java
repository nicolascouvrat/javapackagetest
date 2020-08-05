package com.nikodoko.packagetest.internal;

import com.nikodoko.packagetest.Exported;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

class ExportedBuilder {
  private Path root;
  private Map<String, Map<String, Path>> written = new HashMap<>();

  ExportedBuilder root(Path root) {
    this.root = root;
    return this;
  }

  Path root() {
    return root;
  }

  ExportedBuilder markAsWritten(String module, String fragment, Path path) {
    if (path == null) {
      throw new NullPointerException("marking file with null path as written!");
    }

    Map<String, Path> moduleFiles = written.getOrDefault(module, new HashMap<>());
    moduleFiles.put(fragment, path);
    written.put(module, moduleFiles);
    return this;
  }

  Exported build() {
    return new Exported(root, written);
  }
}
