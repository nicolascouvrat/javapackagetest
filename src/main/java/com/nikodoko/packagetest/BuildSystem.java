package com.nikodoko.packagetest;

import com.nikodoko.packagetest.internal.Exporter;

/** The different types of {@link Exporter} available. */
public enum BuildSystem {
  /** An exporter that generates a typical Maven (multi-module) project structure. */
  MAVEN;
}
