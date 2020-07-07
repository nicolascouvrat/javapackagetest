package com.nikodoko.packagetest;

import com.nikodoko.packagetest.exporters.Exporter;
import com.nikodoko.packagetest.exporters.ExporterFactory;
import com.nikodoko.packagetest.exporters.Kind;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Creates temporary projects on disk to test tools on.
 *
 * <p>{@link #of} makes it easy to create projects for multiple build systems by changing the type
 * of {@link Exporter} used (see {@link Kind} for a list of exporters available).
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
 *     List&#060;Module&#062; modules;
 *     // populate modules with whatever project you want exported
 *     project = Export.of(Kind.MAVEN, modules);
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

  /**
   * Writes a test directory given a kind of exporter and system agnostic module descriptions.
   *
   * <p>Returns an {@link Exported} object containing the results of the export. {@link
   * Exported#cleanup} must be called on the result to remove all created files and folders.
   *
   * @param exporterKind a type of exporter
   * @param modules a list of modules to export
   * @return information about the successful export
   * @throws IOException if an I/O error occurs
   */
  public static Exported of(Kind exporterKind, List<Module> modules) throws IOException {
    Path temp = Files.createTempDirectory(PREFIX);
    Exporter exporter = ExporterFactory.create(exporterKind);
    return exporter.export(modules, temp);
  }
}
