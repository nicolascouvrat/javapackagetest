package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

public interface BzlExpression {
  void write(Writer w) throws IOException;

  void read(PushbackReader r) throws BzlSyntaxError, IOException;

  @FunctionalInterface
  public interface Factory<T extends BzlExpression> {
    T make();
  }
}
