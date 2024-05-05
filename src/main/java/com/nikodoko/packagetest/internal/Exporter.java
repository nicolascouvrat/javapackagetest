package com.nikodoko.packagetest.internal;

import com.nikodoko.packagetest.BuildSystem;
import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Repository;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * {@code Exporter} implementations are responsible for turning generic project descriptions into
 * system specific architectures.
 *
 * <p>See {@link BuildSystem} for a list of exporters available.
 */
public interface Exporter {
  /**
   * Returns the name of this {@code Exporter}.
   *
   * @return the exporter name
   */
  public String name();

  /**
   * Exports a project to a given {@code root} directory.
   *
   * @param modules a list of modules forming a project
   * @param root a path to a root directory
   * @return information about the successful export
   * @throws IOException if an I/O error occurs
   */
  public Exported export(Path root, List<Repository> repositories, List<Module> modules)
      throws IOException;
}
