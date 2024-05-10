package com.nikodoko.packagetest.internal.bazel.grammar;

public class BzlSyntaxError extends Exception {
  BzlSyntaxError(String msg) {
    super(msg);
  }
}
