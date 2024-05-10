package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class BzlSelector implements BzlExpression {
  private List<BzlIdentifier> identifiers;

  public BzlSelector() {}

  public BzlSelector(List<BzlIdentifier> identifiers) {
    this.identifiers = identifiers;
  }

  public List<BzlIdentifier> identifiers() {
    return identifiers;
  }

  @Override
  public void write(Writer w) throws IOException {
    for (int i = 0; i < identifiers.size(); i++) {
      identifiers.get(i).write(w);
      if (i < identifiers.size() - 1) {
        w.write('.');
      }
    }
  }

  @Override
  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    List<BzlIdentifier> ids = new ArrayList<>();
    int next;
    do {
      BzlIdentifier id = new BzlIdentifier();
      id.read(r);
      ids.add(id);

    } while ((char) (next = ReaderUtils.next(r)) == '.');
    // We've read one character too far, put it back
    r.unread(next);

    if (identifiers != null && identifiers.size() != ids.size()) {
      throw new BzlSyntaxError(
          String.format(
              "expected selector of size %d but found %d", identifiers.size(), ids.size()));
    }

    identifiers = ids;
  }
}
