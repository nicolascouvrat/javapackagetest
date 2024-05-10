package com.nikodoko.packagetest.internal.bazel.grammar;

import static com.google.common.truth.Truth.assertThat;

import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Test;

public class BzlStringTest {
  @Test
  public void testRead() throws Exception {
    PushbackReader sr = new PushbackReader(new StringReader("\"java_library\""));
    BzlString str = new BzlString();
    str.read(sr);

    assertThat(str.value()).isEqualTo("java_library");
  }

  @Test
  public void testReadWithWhitespace() throws Exception {
    PushbackReader sr = new PushbackReader(new StringReader("       \n\n   \"java_library\""));
    BzlString str = new BzlString();
    str.read(sr);

    assertThat(str.value()).isEqualTo("java_library");
  }
}
