package com.nikodoko.packagetest.internal.bazel.grammar;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public abstract class BzlCode {
  protected abstract List<BzlStatement> statements();

  public void write(Writer w) throws IOException {
    for (BzlStatement s : statements()) {
      s.write(w);
    }
  }

  public void read(PushbackReader r) throws BzlSyntaxError, IOException {
    for (BzlStatement s : statements()) {
      s.read(r);
    }
  }

  public void write(Path p) throws IOException {
    try (Writer w = Files.newBufferedWriter(p)) {
      write(w);
    }
  }

  public void read(Path p) throws IOException, BzlSyntaxError {
    try (PushbackReader pr = new PushbackReader(Files.newBufferedReader(p))) {
      read(pr);
    }
  }
}
