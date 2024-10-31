package com.nikodoko.packagetest.internal.bazel;

import com.nikodoko.packagetest.internal.bazel.grammar.BzlAssignmentExpression;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlCode;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlExpression;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlFunctionCall;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlList;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlStatement;
import com.nikodoko.packagetest.internal.bazel.grammar.BzlString;
import java.util.Arrays;
import java.util.List;

public class BuildFile extends BzlCode {
  private static final BzlList<BzlString> VISIBILITY_PUBLIC =
      new BzlList<>(BzlString.factory(), List.of(new BzlString("//visibility:public")));

  private final BzlString name;
  private final BzlList<BzlString> srcs;
  private final BzlList<BzlString> deps;
  private final List<BzlStatement> statements;

  private BuildFile(
      BzlString name, BzlList<BzlString> srcs, boolean srcsIsGlob, BzlList<BzlString> deps) {
    this.name = name;
    this.srcs = srcs;
    this.deps = deps;
    this.statements = generateCode(srcsIsGlob);
  }

  private List<BzlStatement> generateCode(boolean srcsIsGlob) {
    BzlExpression srcsExpr = srcsIsGlob ? new BzlFunctionCall("glob", srcs) : srcs;
    return List.of(
        new BzlStatement(
            new BzlFunctionCall(
                "java_library",
                new BzlAssignmentExpression("name", name),
                new BzlAssignmentExpression("srcs", srcsExpr),
                new BzlAssignmentExpression("visibility", VISIBILITY_PUBLIC),
                new BzlAssignmentExpression("deps", deps))));
  }

  public List<String> srcs() {
    return srcs.elements().stream().map(BzlString::value).toList();
  }

  public List<String> deps() {
    return deps.elements().stream().map(BzlString::value).toList();
  }

  public String name() {
    return name.value();
  }

  @Override
  protected List<BzlStatement> statements() {
    return statements;
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private BzlString name = new BzlString();
    private BzlList<BzlString> srcs = new BzlList<>(BzlString.factory());
    private boolean srcsIsGlob = false;
    private BzlList<BzlString> deps = new BzlList<>(BzlString.factory());

    public Builder targetName(String name) {
      this.name = new BzlString(name);
      return this;
    }

    public Builder srcs(String... labels) {
      List<BzlString> sources = Arrays.stream(labels).map(BzlString::new).toList();
      this.srcs = new BzlList<>(BzlString.factory(), sources);
      return this;
    }

    public Builder srcsGlob() {
      this.srcsIsGlob = true;
      return this;
    }

    public Builder deps(String... deps) {
      return deps(Arrays.asList(deps));
    }

    public Builder deps(List<String> deps) {
      List<BzlString> dependencies = deps.stream().map(BzlString::new).toList();
      this.deps = new BzlList<>(BzlString.factory(), dependencies);
      return this;
    }

    public BuildFile build() {
      return new BuildFile(name, srcs, srcsIsGlob, deps);
    }
  }
}
