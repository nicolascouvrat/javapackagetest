package com.nikodoko.packagetest.exporters;

import java.nio.file.Path;

/**
 * {@code Exporter} implementations are responsible for turning generic project descriptions into
 * system specific architectures.
 *
 * <p>See {@link Kind} for a list of exporters available.
 */
public interface Exporter {
  /**
   * Returns the name of this {@code Exporter}.
   *
   * @return the exporter name
   */
  public String name();

  /**
   * Reports the system path of a file, given a root directory, a module and a path fragment.
   *
   * @param root a directory
   * @param module a module name
   * @param fragment a path fragment
   * @return the path of the designated file
   */
  public Path filename(Path root, String module, String fragment);
}
