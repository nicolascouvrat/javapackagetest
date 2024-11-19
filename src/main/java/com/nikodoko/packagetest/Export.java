package com.nikodoko.packagetest;

import com.nikodoko.packagetest.internal.Exporter;
import com.nikodoko.packagetest.internal.ExporterFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Creates temporary projects on disk to test tools on.
 *
 * <p>{@link #of} makes it easy to create projects for multiple build systems by changing the type
 * of {@link Exporter} used (see {@link BuildSystem} for a list of exporters available).
 *
 * <p><b>Example (using Junit)</b>:
 *
 * <blockquote>
 *
 * <pre>
 * public class MyToolTest {
 *   Exported project;
 *
 *   &#064;After
 *   public void cleanup() {
 *     project.cleanup();
 *   }
 *
 *   &#064;Test
 *   public void test() {
 *     // populate modules with whatever project you want exported
 *     Module[] modules;
 *     project = Export.of(BuildSystem.MAVEN, modules);
 *     // now ready to run test on the generated project...
 *   }
 * }
 * </pre>
 *
 * </blockquote>
 *
 * Do not forget to cleanup the generated files using {@link Exported#cleanup}!
 */
public class Export {
  private static final String PREFIX = "packagetest";

  private Export() {}

  /** Options for the export of temporary projects. */
  public static class Options {
    private final Path root;

    private Options(Path root) {
      this.root = root;
    }

    /** The directory in which projects will be created. */
    public Path root() {
      return root;
    }

    public static Builder builder() {
      return new Builder();
    }

    public static Options defaults() {
      return builder().build();
    }

    public static class Builder {
      private Optional<Path> root = Optional.empty();

      /**
       * Sets the directory to use for creating projects.
       *
       * <p>By default, a temporary directory will be created.
       */
      public Builder usingRoot(Path path) {
        this.root = Optional.of(path);
        return this;
      }

      public Options build() {
        try {
          return new Options(root.orElse(Files.createTempDirectory(PREFIX)));
        } catch (IOException e) {
          throw new RuntimeException("Cannot create temporary directory", e);
        }
      }
    }
  }

  /**
   * Writes a test directory given a build system and system agnostic module descriptions.
   *
   * <p>Returns an {@link Exported} object containing the results of the export. {@link
   * Exported#cleanup} must be called on the result to remove all created files and folders.
   *
   * @param buildSystem the build system to use
   * @param modules an array of modules to export
   * @return information about the successful export
   * @throws IOException if an I/O error occurs
   */
  public static Exported of(BuildSystem buildSystem, Module... modules) throws IOException {
    return of(buildSystem, List.of(), Arrays.asList(modules));
  }

  /**
   * Writes a test directory given a build system and system agnostic module descriptions.
   *
   * <p>Returns an {@link Exported} object containing the results of the export. {@link
   * Exported#cleanup} must be called on the result to remove all created files and folders.
   *
   * @param buildSystem the build system to use
   * @param modules an array of modules to export
   * @param repositories an array of repositories containing external dependencies for the modules
   * @return information about the successful export
   * @throws IOException if an I/O error occurs
   */
  public static Exported of(
      BuildSystem buildSystem, List<Repository> repositories, List<Module> modules)
      throws IOException {
    return of(buildSystem, repositories, modules, Options.defaults());
  }

  /**
   * Writes a test directory given a build system and system agnostic module descriptions.
   *
   * <p>Returns an {@link Exported} object containing the results of the export. {@link
   * Exported#cleanup} must be called on the result to remove all created files and folders.
   *
   * @param buildSystem the build system to use
   * @param modules an array of modules to export
   * @param repositories an array of repositories containing external dependencies for the modules
   * @param options options to use for creating projects
   * @return information about the successful export
   * @throws IOException if an I/O error occurs
   */
  public static Exported of(
      BuildSystem buildSystem, List<Repository> repositories, List<Module> modules, Options options)
      throws IOException {
    Exporter exporter = ExporterFactory.create(buildSystem);
    return exporter.export(repositories, modules, options);
  }
}
