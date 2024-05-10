package com.nikodoko.packagetest.internal.bazel.grammar;

import static com.google.common.truth.Truth.assertThat;

import java.io.PushbackReader;
import java.io.StringReader;
import org.junit.Test;

public class BzlFunctionCallTest {
  @Test
  public void testRead() throws Exception {
    PushbackReader sr =
        new PushbackReader(
            new StringReader(
                "java_library(name=\"my.module\",srcs=glob([\"src/main/java/**/*.java\"]),"
                    + "deps=[\"@maven//:com_mycompany_dep_dep\"])"));

    BzlList<BzlString> srcs = new BzlList(BzlString.factory());
    BzlList<BzlString> deps = new BzlList(BzlString.factory());
    BzlString name = new BzlString();
    BzlFunctionCall fc =
        new BzlFunctionCall(
            "java_library",
            new BzlAssignmentExpression("name", name),
            new BzlAssignmentExpression("srcs", new BzlFunctionCall("glob", srcs)),
            new BzlAssignmentExpression("deps", deps));

    fc.read(sr);

    assertThat(srcs.elements()).hasSize(1);
    assertThat(srcs.elements().get(0).value()).isEqualTo("src/main/java/**/*.java");
    assertThat(deps.elements()).hasSize(1);
    assertThat(deps.elements().get(0).value()).isEqualTo("@maven//:com_mycompany_dep_dep");
    assertThat(name.value()).isEqualTo("my.module");
  }

  @Test
  public void testReadWithWhitespace() throws Exception {
    PushbackReader sr =
        new PushbackReader(
            new StringReader(
                "java_library(\nname=\"my.module\",\nsrcs=glob([\"src/main/java/**/*.java\"]),\n"
                    + "deps=[\"@maven//:com_mycompany_dep_dep\"])"));

    BzlList<BzlString> srcs = new BzlList(BzlString.factory());
    BzlList<BzlString> deps = new BzlList(BzlString.factory());
    BzlString name = new BzlString();
    BzlFunctionCall fc =
        new BzlFunctionCall(
            "java_library",
            new BzlAssignmentExpression("name", name),
            new BzlAssignmentExpression("srcs", new BzlFunctionCall("glob", srcs)),
            new BzlAssignmentExpression("deps", deps));

    fc.read(sr);

    assertThat(srcs.elements()).hasSize(1);
    assertThat(srcs.elements().get(0).value()).isEqualTo("src/main/java/**/*.java");
    assertThat(deps.elements()).hasSize(1);
    assertThat(deps.elements().get(0).value()).isEqualTo("@maven//:com_mycompany_dep_dep");
    assertThat(name.value()).isEqualTo("my.module");
  }
}
