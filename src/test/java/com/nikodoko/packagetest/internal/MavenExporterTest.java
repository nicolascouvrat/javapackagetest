package com.nikodoko.packagetest.internal;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import com.google.common.truth.Correspondence;
import com.nikodoko.packagetest.BuildSystem;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.DefaultModelReader;
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
    Module anAwesomeModule =
        Module.named("an.awesome.module")
            .containing(
                Module.file("a/A.java", "package an.awesome.module.a;"),
                Module.file("a/ATest.java", "package an.awesome.module.a;"),
                Module.file("b/B.java", "package an.awesome.module.b;"));
    Module anOtherModule =
        Module.named("an.other.module")
            .containing(Module.file("C.java", "package an.other.module;"))
            .dependingOn(
                Module.dependency("my.dependency", "a-dependency"),
                Module.dependency("my.dependency", "another-dependency", "1.0"));

    try {
      out = Export.of(BuildSystem.MAVEN, anAwesomeModule, anOtherModule);
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
    checkWritten(out, "an.awesome.module", "pom.xml", "anawesomemodule/pom.xml");
    checkWritten(out, "an.other.module", "pom.xml", "anothermodule/pom.xml");

    checkContent(out, "an.awesome.module", "a/A.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "a/ATest.java", "package an.awesome.module.a;");
    checkContent(out, "an.awesome.module", "b/B.java", "package an.awesome.module.b;");
    checkContent(out, "an.other.module", "C.java", "package an.other.module;");
    checkPomContent(out, "an.awesome.module", checkDependencies());
    checkPomContent(
        out,
        "an.other.module",
        checkDependencies(
            "my.dependency", "a-dependency", null, "my.dependency", "another-dependency", null),
        checkDependencyManagement(
            "my.dependency", "another-dependency", "${another-dependency.version}"),
        checkProperties("another-dependency.version", "1.0"));
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

  private void checkPomContent(Exported result, String module, Consumer<Model>... checkers)
      throws Exception {
    Path written = getFile(result, module, "pom.xml");
    Model model = new DefaultModelReader().read(written.toFile(), null);
    for (Consumer<Model> checker : checkers) {
      checker.accept(model);
    }
  }

  private Path getFile(Exported result, String module, String fragment) {
    Optional<Path> written = result.file(module, fragment);
    if (!written.isPresent()) {
      fail("file " + fragment + " not written for module " + module);
    }

    return written.get();
  }

  private Consumer<Model> checkDependencies(String... params) {
    return model ->
        assertThat(model.getDependencies())
            .comparingElementsUsing(
                Correspondence.from(this::dependenciesEquivalent, "equivalent to"))
            .containsExactlyElementsIn(dependenciesFromParams(params));
  }

  private Consumer<Model> checkProperties(String... properties) {
    return model -> {
      Properties props = model.getProperties();
      for (int i = 0; i < properties.length; i = i + 2) {
        assertWithMessage("wrong property value for " + properties[i])
            .that(props.getProperty(properties[i]))
            .isEqualTo(properties[i + 1]);
      }
    };
  }

  private Consumer<Model> checkDependencyManagement(String... params) {
    return model ->
        assertThat(model.getDependencyManagement().getDependencies())
            .comparingElementsUsing(
                Correspondence.from(this::dependenciesEquivalent, "equivalent to"))
            .containsExactlyElementsIn(dependenciesFromParams(params));
  }

  private List<Dependency> dependenciesFromParams(String... params) {
    List<Dependency> deps = new ArrayList<>();
    for (int i = 0; i < params.length; i = i + 3) {
      Dependency dep = new Dependency();
      dep.setGroupId(params[i]);
      dep.setArtifactId(params[i + 1]);
      dep.setVersion(params[i + 2]);
      deps.add(dep);
    }

    return deps;
  }

  // There is not implementation of equals() in the Dependency class
  private boolean dependenciesEquivalent(@Nullable Dependency a, @Nullable Dependency b) {
    if ((a == null) != (b == null)) {
      return false;
    }

    if (a == null) {
      return true;
    }

    return Objects.equals(a.getGroupId(), b.getGroupId())
        && Objects.equals(a.getArtifactId(), b.getArtifactId())
        && Objects.equals(a.getVersion(), b.getVersion());
  }
}
