package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

public class BzlAssignmentExpression implements BzlExpression {
  private final BzlIdentifier identifier;
  private final BzlExpression expression;

  public BzlAssignmentExpression(String identifier, BzlExpression expression) {
    this.identifier = new BzlIdentifier(identifier);
    this.expression = expression;
  }

  @Override
  public void write(Writer w) throws IOException {
    identifier.write(w);
    w.write('=');
    expression.write(w);
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    identifier.read(r);
    ReaderUtils.expectNext(r, '=');
    expression.read(r);
  }
}
