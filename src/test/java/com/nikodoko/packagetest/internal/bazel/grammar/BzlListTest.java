package com.nikodoko.packagetest.internal.bazel.grammar;

import static com.google.common.truth.Truth.assertThat;

import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Test;

public class BzlListTest {
  @Test
  public void testRead() throws Exception {
    PushbackReader sr =
        new PushbackReader(new StringReader("[\"//pkg1:target1\",\"//pkg2:target2\"]"));
    BzlList<BzlString> l = new BzlList(BzlString.factory());
    l.read(sr);

    assertThat(l.elements()).hasSize(2);
    assertThat(l.elements().get(0).value()).isEqualTo("//pkg1:target1");
    assertThat(l.elements().get(1).value()).isEqualTo("//pkg2:target2");
  }

  @Test
  public void testReadWithWhitespace() throws Exception {
    PushbackReader sr =
        new PushbackReader(
            new StringReader("  [\n  \"//pkg1:target1\",\n  \"//pkg2:target2\"\n ]"));
    BzlList<BzlString> l = new BzlList(BzlString.factory());
    l.read(sr);

    assertThat(l.elements()).hasSize(2);
    assertThat(l.elements().get(0).value()).isEqualTo("//pkg1:target1");
    assertThat(l.elements().get(1).value()).isEqualTo("//pkg2:target2");
  }
}
