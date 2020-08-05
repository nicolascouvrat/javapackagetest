package com.nikodoko.packagetest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Contains the result of {@link com.nikodoko.packagetest.Export#of}. */
public class Exported {
  // Denotes that cleanup has been already done
  private static final Path EMPTY = Paths.get("");

  private Path root;
  private Map<String, Map<String, Path>> written;

  public Exported(Path root, Map<String, Map<String, Path>> written) {
    this.root = root;
    this.written = written;
  }

  /** Returns the directory at the root of this {@code Exported} data. */
  public Path root() {
    return root;
  }

  /**
   * Returns an optional containing the path for a given module and fragment.
   *
   * @param module a module name
   * @param fragment a path fragment
   */
  public Optional<Path> file(String module, String fragment) {
    Map<String, Path> moduleFiles = written.get(module);
    if (moduleFiles == null) {
      return Optional.empty();
    }

    return Optional.ofNullable(moduleFiles.get(fragment));
  }

  /**
   * Removes the directory at the root of this {@code Exported} and all its contents.
   *
   * <p>This is safe to call multiple times.
   *
   * @throws IOException if an I/O error occurs
   */
  public void cleanup() throws IOException {
    if (root == EMPTY) {
      return;
    }

    Files.walk(root).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);

    root = EMPTY;
    written = new HashMap<>();
  }
}
