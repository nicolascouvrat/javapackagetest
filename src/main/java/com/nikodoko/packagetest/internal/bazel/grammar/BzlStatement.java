package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

public class BzlStatement {
  private final BzlExpression expression;

  public BzlStatement(BzlExpression expression) {
    this.expression = expression;
  }

  public void write(Writer w) throws IOException {
    expression.write(w);
    w.write(System.lineSeparator());
  }

  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    expression.read(r);
    // no need to read the line separator, it's done automatically inside the expressions.
  }
}
