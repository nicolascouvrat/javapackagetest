package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;

public class BzlIdentifier implements BzlExpression {
  private String value;

  public BzlIdentifier() {}

  public BzlIdentifier(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  @Override
  public void write(Writer w) throws IOException {
    w.write(value);
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    int n = ReaderUtils.next(r);
    // Java's definition of identifier is not exactly the same as bazel's but close enough
    if (!Character.isUnicodeIdentifierStart(n)) {
      throw new BzlSyntaxError(String.format("invalid identifier start: %s", (char) n));
    }

    StringBuffer sb = new StringBuffer();
    do {
      sb.append((char) n);
      n = r.read();
    } while (Character.isUnicodeIdentifierPart(n));
    r.unread(n);

    String read = sb.toString();
    if (value != null && !value.equals(read)) {
      throw new BzlSyntaxError(String.format("expected identifier %s but found %s", value, read));
    }

    value = read;
  }
}
