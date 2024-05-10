package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

public class BzlString implements BzlExpression {
  private String value;

  public BzlString() {}

  public BzlString(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public void write(Writer w) throws IOException {
    w.write('"');
    w.write(value);
    w.write('"');
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    ReaderUtils.expectNext(r, '"');

    char c;
    StringBuffer sb = new StringBuffer();
    while ((c = (char) r.read()) != '"') {
      sb.append(c);
    }

    String read = sb.toString();
    if (value != null && !value.equals(read)) {
      throw new BzlSyntaxError(
          String.format("expected string \"%s\" but found \"%s\"", value, read));
    }

    value = read;
  }

  public static BzlExpression.Factory<BzlString> factory() {
    return () -> new BzlString();
  }
}
