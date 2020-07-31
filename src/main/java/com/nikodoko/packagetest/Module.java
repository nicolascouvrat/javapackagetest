package com.nikodoko.packagetest;

import java.util.Arrays;

/**
 * A system agnostic description of a java module.
 *
 * <p>For example, consider the following structure:
 *
 * <pre>
 * com
 *  |
 *  - package
 *    |
 *    - name
 *      |
 *      - A.java
 *      - util
 *        |
 *        - B.java
 * </pre>
 *
 * This can be described as a {@code Module} of name {@code "com.package.name"} and containing two
 * {@link File} designated by their path fragment ({@code "A.java"} for one and {@code
 * "util/B.java"}) for the other.
 */
public class Module {
  private final String name;
  private File[] files = new File[] {};
  private Dependency[] dependencies = new Dependency[] {};

  private Module(String name) {
    this.name = name;
  }

  /** Returns a {@code Module} named {@code name} and containing no files. */
  public static Module named(String name) {
    return new Module(name);
  }

  /** Replaces files contained in this {@code Module} by {@code files}. */
  public Module containing(File... files) {
    this.files = files;
    return this;
  }

  /** Replaces dependencies of this {@code Module} by {@code dependencies}. */
  public Module dependingOn(Dependency... dependencies) {
    this.dependencies = dependencies;
    return this;
  }

  /**
   * Returns a file located at the relative path given by {@code fragment} and containing {@code
   * contents}.
   */
  public static File file(String fragment, String contents) {
    return new File(fragment, contents);
  }

  /**
   * Return a dependency with the given {@code groupdId}, {@code artifactId} and {@code version}.
   */
  public static Dependency dependency(String groupId, String artifactId, String version) {
    return new Dependency(groupId, artifactId, version);
  }

  /** Return a dependency with the given {@code groupdId}, {@code artifactId} and no version. */
  public static Dependency dependency(String groupId, String artifactId) {
    return new Dependency(groupId, artifactId, "");
  }

  /** The name of this {@code Module}. */
  public String name() {
    return name;
  }

  /** Returns an iterable view of all the files contained in this module. */
  public Iterable<File> files() {
    return Arrays.asList(files);
  }

  /** A system agnostic description of a dependency. */
  public static class Dependency {
    private final String groupId;
    private final String artifactId;
    private final String version;

    /** This {@code Dependency}'s groupId. */
    public String groupId() {
      return groupId;
    }

    /** This {@code Dependency}'s artifactId. */
    public String artifactId() {
      return artifactId;
    }

    /** This {@code Dependency}'s version. */
    public String version() {
      return version;
    }

    private Dependency(String groupId, String artifactId, String version) {
      this.groupId = groupId;
      this.artifactId = artifactId;
      this.version = version;
    }
  }

  /** A system agnostic description of a file in a Java module. */
  public static class File {
    private final String fragment;
    private final String contents;

    /**
     * A path fragment pointing to this {@code File}.
     *
     * <p>This is essentially the relative path to this file from the module root. For instance, if
     * the folder structure is {@code com/package/prefix/package/File.java}, then {@code fragment()}
     * will return {@code "package/File.java"}.
     */
    public String fragment() {
      return fragment;
    }

    /** This {@code File}'s contents. */
    public String contents() {
      return contents;
    }

    private File(String fragment, String contents) {
      this.fragment = fragment;
      this.contents = contents;
    }
  }
}
