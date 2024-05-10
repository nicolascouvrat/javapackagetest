package com.nikodoko.packagetest.internal.bazel.grammar;

import static com.google.common.truth.Truth.assertThat;

import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Test;

public class BzlAssignmentExpressionTest {
  @Test
  public void testRead() throws Exception {
    PushbackReader sr = new PushbackReader(new StringReader("artifacts=[\"//pkg1:target1\"]"));
    BzlList<BzlString> l = new BzlList(BzlString.factory());
    BzlAssignmentExpression ae = new BzlAssignmentExpression("artifacts", l);

    ae.read(sr);

    assertThat(l.elements()).hasSize(1);
    assertThat(l.elements().get(0).value()).isEqualTo("//pkg1:target1");
  }

  @Test
  public void testReadWithWhitespace() throws Exception {
    PushbackReader sr =
        new PushbackReader(new StringReader("   artifacts = \n [\"//pkg1:target1\"]"));
    BzlList<BzlString> l = new BzlList(BzlString.factory());
    BzlAssignmentExpression ae = new BzlAssignmentExpression("artifacts", l);

    ae.read(sr);

    assertThat(l.elements()).hasSize(1);
    assertThat(l.elements().get(0).value()).isEqualTo("//pkg1:target1");
  }
}
