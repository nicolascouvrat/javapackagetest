package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class BzlList<T extends BzlExpression> implements BzlExpression {
  private List<T> elements;
  private final BzlExpression.Factory<T> factory;

  public BzlList(BzlExpression.Factory<T> factory) {
    this.factory = factory;
  }

  public BzlList(BzlExpression.Factory<T> factory, List<T> elements) {
    this.factory = factory;
    this.elements = elements;
  }

  public List<T> elements() {
    return elements;
  }

  @Override
  public void write(Writer w) throws IOException {
    w.write('[');
    for (T elt : elements) {
      elt.write(w);
      w.write(',');
    }

    w.write(']');
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    ReaderUtils.expectNext(r, '[');
    List<T> elts = new ArrayList<>();
    do {
      T elt = factory.make();
      elt.read(r);
      elts.add(elt);
    } while (hasNextElt(r));

    char next = (char) ReaderUtils.next(r);
    if (next != ']') {
      throw new BzlSyntaxError(String.format("expected ']' but found '%s'", next));
    }

    if (elements != null && elements.size() != elts.size()) {
      throw new BzlSyntaxError(
          String.format("expected list of size %d but found %d", elements.size(), elts.size()));
    }

    elements = elts;
  }

  private boolean hasNextElt(PushbackReader r) throws BzlSyntaxError, IOException {
    int next = ReaderUtils.next(r);
    if ((char) next != ',') {
      // done, push it back
      r.unread(next);
      return false;
    }

    // Account for trailing commas: we only have a next element if the next character is not ]
    next = ReaderUtils.next(r);
    r.unread(next);
    if ((char) next == ']') {
      return false;
    }

    return true;
  }
}
