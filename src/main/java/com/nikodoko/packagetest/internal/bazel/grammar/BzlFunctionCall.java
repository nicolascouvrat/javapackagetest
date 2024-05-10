package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.Arrays;

public class BzlFunctionCall implements BzlExpression {
  private final BzlExpression[] arguments;
  private final BzlSelector selector;

  public BzlFunctionCall(String identifier, BzlExpression... arguments) {
    this.selector =
        new BzlSelector(Arrays.stream(identifier.split("\\.")).map(BzlIdentifier::new).toList());
    this.arguments = arguments;
  }

  @Override
  public void write(Writer w) throws IOException {
    selector.write(w);
    w.write('(');
    for (BzlExpression arg : arguments) {
      arg.write(w);
      w.write(',');
    }

    w.write(')');
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    selector.read(r);

    ReaderUtils.expectNext(r, '(');
    for (int i = 0; i < arguments.length; i++) {
      arguments[i].read(r);
      if (i != arguments.length - 1) {
        ReaderUtils.expectNext(r, ',');
      }
    }

    // We can end with `)`, or with `,)`
    char next = (char) ReaderUtils.next(r);
    switch (next) {
      case ',':
        ReaderUtils.expectNext(r, ')');
        break;
      case ')':
        break;
      default:
        throw new BzlSyntaxError(String.format("invalid end of function call '%s'", next));
    }
  }
}
