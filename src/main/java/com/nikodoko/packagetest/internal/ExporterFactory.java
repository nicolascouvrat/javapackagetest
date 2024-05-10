package com.nikodoko.packagetest.internal;

import com.nikodoko.packagetest.BuildSystem;

/** An {@link Exporter} factory. */
public class ExporterFactory {
  private ExporterFactory() {}

  /** Creates an {@link Exporter} for a given {@link BuildSystem}. */
  public static Exporter create(BuildSystem type) {
    switch (type) {
      case BAZEL:
        return new BazelExporter();
      case MAVEN:
        return new MavenExporter();
      default:
        throw new IllegalArgumentException("unknown build system: " + type);
    }
  }
}
