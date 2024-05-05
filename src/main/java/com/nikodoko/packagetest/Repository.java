package com.nikodoko.packagetest;

/** A system agnostic description of a java repository. */
public class Repository {
  private final String name;
  private String url = "";

  private Repository(String name) {
    this.name = name;
  }

  public static Repository named(String name) {
    return new Repository(name);
  }

  public Repository at(String url) {
    this.url = url;
    return this;
  }
}
