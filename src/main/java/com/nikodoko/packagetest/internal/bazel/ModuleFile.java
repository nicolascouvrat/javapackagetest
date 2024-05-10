package com.nikodoko.packagetest.internal.bazel;

import com.nikodoko.packagetest.internal.bazel.grammar.BzlAssignmentExpression;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlCode;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlFunctionCall;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlIdentifier;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlList;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlStatement;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlString;
import java.util.Arrays;
import java.util.List;

public class ModuleFile extends BzlCode {
  public static final String RULES_JVM_EXTERNAL_VERSION = "6.1";
  private static final BzlFunctionCall BAZEL_DEP =
      new BzlFunctionCall(
          "bazel_dep",
          new BzlAssignmentExpression("name", new BzlString("rules_jvm_external")),
          new BzlAssignmentExpression("version", new BzlString(RULES_JVM_EXTERNAL_VERSION)));
  private static final BzlFunctionCall USE_EXTENSION =
      new BzlFunctionCall(
          "use_extension",
          new BzlString("@rules_jvm_external//:extensions.bzl"),
          new BzlString("maven"));
  private static final BzlFunctionCall USE_REPO =
      new BzlFunctionCall("use_repo", new BzlIdentifier("maven"), new BzlString("maven"));

  private final BzlList<BzlString> artifacts;
  private final BzlList<BzlString> repositories;
  private final List<BzlStatement> statements;

  private ModuleFile(BzlList<BzlString> artifacts, BzlList<BzlString> repositories) {
    this.artifacts = artifacts;
    this.repositories = repositories;
    this.statements = generateCode();
  }

  public List<String> repositories() {
    return repositories.elements().stream().map(BzlString::value).toList();
  }

  public List<String> artifacts() {
    return artifacts.elements().stream().map(BzlString::value).toList();
  }

  private List<BzlStatement> generateCode() {
    BzlFunctionCall mvnInstall =
        new BzlFunctionCall(
            "maven.install",
            new BzlAssignmentExpression("artifacts", artifacts),
            new BzlAssignmentExpression("fetch_sources", new BzlIdentifier("True")),
            new BzlAssignmentExpression("repositories", repositories));
    return List.of(
        new BzlStatement(BAZEL_DEP),
        new BzlStatement(new BzlAssignmentExpression("maven", USE_EXTENSION)),
        new BzlStatement(mvnInstall),
        new BzlStatement(USE_REPO));
  }

  @Override
  protected List<BzlStatement> statements() {
    return statements;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private BzlList<BzlString> artifacts = new BzlList<>(BzlString.factory());
    private BzlList<BzlString> repositories = new BzlList<>(BzlString.factory());

    public Builder artifacts(String... deps) {
      return artifacts(Arrays.asList(deps));
    }

    public Builder artifacts(List<String> deps) {
      List<BzlString> artifacts = deps.stream().map(BzlString::new).toList();
      this.artifacts = new BzlList<>(BzlString.factory(), artifacts);
      return this;
    }

    public Builder repositories(String... repos) {
      return repositories(Arrays.asList(repos));
    }

    public Builder repositories(List<String> repos) {
      List<BzlString> repositories = repos.stream().map(BzlString::new).toList();
      this.repositories = new BzlList<>(BzlString.factory(), repositories);
      return this;
    }

    public ModuleFile build() {
      return new ModuleFile(artifacts, repositories);
    }
  }
}
