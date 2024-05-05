package com.nikodoko.packagetest.internal;

import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

class BazelFileWriter {
  private static final String MODULE_TEMPLATE =
      """
      bazel_dep(name = "rules_jvm_external", version = "5.3")

      maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
      maven.install(
          artifacts = [
              %s
          ],
          fetch_sources = True,
          repositories = [
              %s
          ],
      )
      use_repo(maven, "maven")
      """;
  private static final String BUILD_TEMPLATE =
      """
      java_library(
          name = "%s",
          srcs = glob([
              "%s",
          ]),
          resources = glob([]),
          deps = [
              %s
          ],
      )
      """;
  private static final String JAVA_FILES_GLOB_TEMPLATE = "%s/**/*.java";

  private static String toIndentedList(List<String> lines, int indent) {
    var delimiter = "," + System.lineSeparator() + " ".repeat(indent);
    return String.join(delimiter, lines);
  }

  static void writeModule(Path target, Set<Module.Dependency> deps) throws IOException {
    var flatDeps = toIndentedList(deps.stream().map(BazelFileWriter::toCoordinates).toList(), 8);
    var formatted = String.format(MODULE_TEMPLATE, flatDeps, "");
    Files.writeString(target, formatted, StandardCharsets.UTF_8);
  }

  static void writeBuild(Path target, Module mod, String srcDir) throws IOException {
    var bazelDeps =
        StreamSupport.stream(mod.dependencies().spliterator(), false)
            .map(BazelFileWriter::toBazelDep)
            .toList();
    var formatted =
        String.format(
            BUILD_TEMPLATE,
            mod.name(),
            String.format(JAVA_FILES_GLOB_TEMPLATE, srcDir),
            toIndentedList(bazelDeps, 8));
    Files.writeString(target, formatted, StandardCharsets.UTF_8);
  }

  private static String toCoordinates(Module.Dependency d) {
    return "\"%s:%s:%s\"".formatted(d.groupId(), d.artifactId(), d.version());
  }

  private static String toBazelDep(Module.Dependency d) {
    return "\"@maven//:%s_%s\""
        .formatted(
            d.groupId().replace('-', '_').replace('.', '_'),
            d.artifactId().replace('-', '_').replace('.', '_'));
  }
}
