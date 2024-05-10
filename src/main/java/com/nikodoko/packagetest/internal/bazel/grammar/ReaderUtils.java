package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.Reader;

public class ReaderUtils {
  static int next(Reader r) throws BzlSyntaxError, IOException {
    int n;
    while ((n = r.read()) != -1) {
      if (Character.isWhitespace(n)) {
        continue;
      }

      return n;
    }

    throw new BzlSyntaxError(String.format("expected non whitespace char but found end of stream"));
  }

  static void expectNext(Reader r, char expected) throws BzlSyntaxError, IOException {
    char next = (char) next(r);
    if (next == expected) {
      return;
    }

    throw new BzlSyntaxError(String.format("expected '%s' but found '%s'", expected, next));
  }
}
