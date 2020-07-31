package com.nikodoko.packagetest.exporters;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.nikodoko.packagetest.BuildSystem;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.After;
import org.junit.Test;

public class MavenExporterTest {
  Exported out;

  @After
  public void cleanup() throws Exception {
    out.cleanup();
  }

  @Test
  public void testExport() throws Exception {
    List<Module> modules =
        ImmutableList.of(
            new Module(
                "an.awesome.module",
                ImmutableMap.of(
                    "a/A.java",
                    "package an.awesome.module.a;",
                    "a/ATest.java",
                    "package an.awesome.module.a;",
                    "b/B.java",
                    "package an.awesome.module.b;")),
            new Module("an.other.module", ImmutableMap.of("C.java", "package an.other.module;")));

    try {
      out = Export.of(BuildSystem.MAVEN, modules);
    } catch (IOException e) {
      fail(e.getMessage());
    }

    checkWritten(
        out,
        "an.awesome.module",
        "a/A.java",
        "anawesomemodule/src/main/java/an/awesome/module/a/A.java");
    checkWritten(
        out,
        "an.awesome.module",
        "a/ATest.java",
        "anawesomemodule/src/test/java/an/awesome/module/a/ATest.java");
    checkWritten(
        out,
        "an.awesome.module",
        "b/B.java",
        "anawesomemodule/src/main/java/an/awesome/module/b/B.java");
    checkWritten(
        out, "an.other.module", "C.java", "anothermodule/src/main/java/an/other/module/C.java");

    checkContent(out, "an.awesome.module", "a/A.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "a/ATest.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "b/B.java", "package an.awesome.module.b;");
    checkContent(out, "an.other.module", "C.java", "package an.other.module;");
  }

  private void checkContent(Exported result, String module, String fragment, String expected)
      throws Exception {
    Optional<Path> written = result.file(module, fragment);
    if (!written.isPresent()) {
      fail("file " + fragment + " not written for module " + module);
    }

    String got = null;
    try {
      got = new String(Files.readAllBytes(written.get()), UTF_8);
    } catch (IOException e) {
      fail("cannot read file " + written);
    }

    assertThat(got).isEqualTo(expected);
  }

  private void checkWritten(Exported result, String module, String fragment, String expected)
      throws Exception {
    Path expect = result.root().resolve(expected);
    Optional<Path> got = result.file(module, fragment);
    if (!got.isPresent()) {
      fail("file " + fragment + " not written for module " + module);
    }

    assertThat((Object) got.get()).isEqualTo(expect);
  }
}
