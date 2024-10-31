package com.nikodoko.packagetest.internal;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import com.nikodoko.packagetest.BuildSystem;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Repository;
import com.nikodoko.packagetest.internal.bazel.BuildFile;
import com.nikodoko.packagetest.internal.bazel.ModuleFile;
import java.io.PushbackReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.After;
import org.junit.Test;

public class BazelExporterTest {
  Exported out;

  @After
  public void cleanup() throws Exception {
    // out.cleanup();
  }

  @Test
  public void testExport() throws Exception {
    Module anOtherModule =
        Module.named("an.other.module")
            .containing(Module.file("C.java", "package an.other.module;"))
            .dependingOn(Module.dependency("com.mycompany.app", "another-dependency", "1.0"));
    Module anAwesomeModule =
        Module.named("an.awesome.module")
            .containing(
                Module.file("a/A.java", "package an.awesome.module.a;"),
                Module.file("a/ATest.java", "package an.awesome.module.a;"),
                Module.file("b/B.java", "package an.awesome.module.b;"))
            .dependingOn(anOtherModule);
    Repository repo = Repository.named("local").at("file:///Users/nicolas.couvrat/.m2/repository");
    Repository repoCentral = Repository.named("central").at("https://repo1.maven.org/maven2");

    out =
        Export.of(
            BuildSystem.BAZEL, List.of(repo, repoCentral), List.of(anAwesomeModule, anOtherModule));
    System.out.println(out.root());

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
    checkWritten(out, "an.awesome.module", "BUILD.bazel", "anawesomemodule/BUILD.bazel");
    checkWritten(out, "an.other.module", "BUILD.bazel", "anothermodule/BUILD.bazel");
    checkWritten(out, "", "MODULE.bazel", "MODULE.bazel");

    checkContent(out, "an.awesome.module", "a/A.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "a/ATest.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "b/B.java", "package an.awesome.module.b;");
    checkContent(out, "an.other.module", "C.java", "package an.other.module;");
    checkBuildContent(
        out,
        "an.awesome.module",
        checkBuildDeps("//anothermodule:an.other.module"),
        checkBuildSrcs("src/main/java/**/*.java"));
    checkBuildContent(
        out,
        "an.other.module",
        checkBuildDeps("@maven//:com_mycompany_app_another_dependency"),
        checkBuildSrcs("src/main/java/**/*.java"));
    checkModuleContent(
        out,
        checkModuleDeps("com.mycompany.app:another-dependency:1.0"),
        checkModuleRepos(
            "file:///Users/nicolas.couvrat/.m2/repository", "https://repo1.maven.org/maven2"));
  }

  private void checkContent(Exported result, String module, String fragment, String expected)
      throws Exception {
    Path written = getFile(result, module, fragment);
    String got = new String(Files.readAllBytes(written), UTF_8);
    assertThat(got).isEqualTo(expected);
  }

  private void checkWritten(Exported result, String module, String fragment, String expected)
      throws Exception {
    Path expect = result.root().resolve(expected);
    Path got = getFile(result, module, fragment);
    assertThat((Object) got).isEqualTo(expect);
  }

  private void checkBuildContent(Exported result, String module, Consumer<BuildFile>... checkers)
      throws Exception {
    Path written = getFile(result, module, "BUILD.bazel");
    // Expected sources as glob
    BuildFile file = BuildFile.builder().srcsGlob().build();
    file.read(new PushbackReader(Files.newBufferedReader(written)));

    assertThat(file.name()).isEqualTo(module);
    for (Consumer<BuildFile> checker : checkers) {
      checker.accept(file);
    }
  }

  private void checkModuleContent(Exported result, Consumer<ModuleFile>... checkers)
      throws Exception {
    Path written = getFile(result, "", "MODULE.bazel");
    // Expected sources as glob
    ModuleFile file = ModuleFile.builder().build();
    try {
      file.read(new PushbackReader(Files.newBufferedReader(written)));
    } catch (Exception e) {
      System.out.println(new String(Files.readAllBytes(written), UTF_8));
      throw e;
    }

    for (Consumer<ModuleFile> checker : checkers) {
      checker.accept(file);
    }
  }

  private Path getFile(Exported result, String module, String fragment) {
    Optional<Path> written = result.file(module, fragment);
    if (!written.isPresent()) {
      fail("file " + fragment + " not written for module " + module);
    }

    return written.get();
  }

  private Consumer<BuildFile> checkBuildDeps(String... expected) {
    return f -> assertThat(f.deps()).containsExactlyElementsIn(expected);
  }

  private Consumer<BuildFile> checkBuildSrcs(String... expected) {
    return f -> assertThat(f.srcs()).containsExactlyElementsIn(expected);
  }

  private Consumer<ModuleFile> checkModuleDeps(String... expected) {
    return f -> assertThat(f.artifacts()).containsExactlyElementsIn(expected);
  }

  private Consumer<ModuleFile> checkModuleRepos(String... expected) {
    return f -> assertThat(f.repositories()).containsExactlyElementsIn(expected);
  }
}
