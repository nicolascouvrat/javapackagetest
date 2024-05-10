# Java Package Test

`Javapackagetest` is a lightweight library that makes creating temporary projects on disk to test
tools on a breeze.

## How it works

The main goal of `javapackagetest` is to facilitate testing utilities on different build systems,
granted that they all come with a different "standard" in terms of folder organization.

It works by converting a build system agnostic description of a Java package (a list of `Module`s)
into a build system specific architecture using `Exporter`s.

The main entry point of the library is `Export.of`, that returns a view of the generated
architecture as an `Exported` object. When done, do not forget to call `Exported.cleanup()`
to erase the temporary project!

## Supported Build Systems 

* `BuildSystem.MAVEN`
* `BuildSystem.BAZEL`

## Examples

### Basic Example

#### Maven

```java

package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
                Module.file("b/B.java", "package my.first.module.b"));
    Module m2 =
        Module.named("my.second.module")
            .containing(Module.file("C.java", "package my.second.module"));

    project = Export.of(BuildSystem.MAVEN, m1, m2);
    // You now how the following structure to work with
    // some_temporary_folder
    //  |
    //  - myfirstmodule
    //  | |
    //  | - pom.xml
    //  | - src/main/java/my/first/module
    //  |   |
    //  |   - A.java
    //  |   - b
    //  |     |
    //  |     - B.java
    //  - mysecondmodule
    //    |
    //    - pom.xml
    //    - src/main/java/my/second/module
    //      |
    //      - C.java
  }
}
```

#### Bazel

```java

package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
                Module.file("b/B.java", "package my.first.module.b"));
    Module m2 =
        Module.named("my.second.module")
            .containing(Module.file("C.java", "package my.second.module"));

    project = Export.of(BuildSystem.BAZEL, m1, m2);
    // You now how the following structure to work with
    // some_temporary_folder
    //  - MODULE.bazel
    //  - myfirstmodule
    //  | |
    //  | - BUILD.bazel
    //  | - src/main/java/my/first/module
    //  |   |
    //  |   - A.java
    //  |   - b
    //  |     |
    //  |     - B.java
    //  - mysecondmodule
    //    |
    //    - BUILD.bazel
    //    - src/main/java/my/second/module
    //      |
    //      - C.java
  }
}
```
### Exporting Test Files

All files ending in `Test.java` will be placed in the usual test location for the chosen build system.

#### Maven

```java
package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
                Module.file("ATest.java", "package my.first.module"));

    project = Export.of(BuildSystem.MAVEN, m1);
    // You now how the following structure to work with
    // some_temporary_folder
    //  |
    //  - myfirstmodule
    //  | |
    //  | - pom.xml
    //  | - src/main/java/my/first/module
    //  |   |
    //  |   - A.java
    //  | - src/test/java/my/first/module
    //      |
    //      - ATest.java
  }
}
```

#### Bazel

Not supported yet.

### Faking Dependencies

Dependencies will be added to the template files specific to the chosen build system.

#### Maven

```java
package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
            .dependingOn(
                Module.dependency("my.other.module", "other-module", "1.1.1"),
                Module.dependency("com.google.guava", "guava"));

    project = Export.of(BuildSystem.MAVEN, m1);
    // The resulting pom.xml in myfirstmodule will contain the following:
    //  <properties>
    //    <other-module.version>1.1.1</other-module.version>
    //  </properties>
    //  <dependencyManagement>
    //    <dependencies>
    //      <dependency>
    //        <groupId>my.other.module</groupId>
    //        <artifactId>other-module</artifactId>
    //        <version>${other-module.version}</version>
    //      </dependency>
    //    </dependencies>
    //  </dependencyManagement>
    //  <dependencies>
    //    <dependency>
    //      <groupId>my.other.module</groupId>
    //      <artifactId>other-module</artifactId>
    //    </dependency>
    //    <dependency>
    //      <groupId>com.google.guava</groupId>
    //      <artifactId>guava</artifactId>
    //    </dependency>
    //  </dependencies>
  }
}
```

#### Bazel

```java
package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
            .dependingOn(
                Module.dependency("my.other.module", "other-module", "1.1.1"),
                Module.dependency("com.google.guava", "guava", "1.0"));

    project = Export.of(BuildSystem.BAZEL, m1);
    // The resulting MODULE.bazel at the root will contain the following:
    // bazel_dep(name = "rules_jvm_external", version = "6.1")
    // 
    // maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
    // maven.install(
    //     artifacts = ["my.other.module:other-module:1.1.1", "com.google.guava:guava:1.0"],
    //     fetch_sources = True,
    //     repositories = [],
    // )
    // use_repo(maven, "maven")
    //
    // The resulting BUILD.bazel in myfirstmodule will contain the following:
    // java_library(
    //     name = "an.other.module",
    //     srcs = glob(["src/main/java/**/*.java"]),
    //     deps = ["@maven//:my_other_module_other_module", "@maven//:com_google_guava_guava"],
    // )
  }
}
```

### Specifying repositories

You can specify which repository to use for your dependencies, which can be useful in order to make
the resulting project compile.

#### Maven

Not supported yet.

#### Bazel

```java
package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.BuildSystem;
import org.junit.Test;
import org.junit.After;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    Module m1 =
        Module.named("my.first.module")
            .containing(
                Module.file("A.java", "package my.first.module"),
            .dependingOn(
                Module.dependency("my.other.module", "other-module", "1.1.1"),
                Module.dependency("com.google.guava", "guava", "1.0"));
    Repository repo1 = Repository.named("local").at("file:///Users/nicolas.couvrat/.m2/repository");
    Repository repo2 = Repository.named("central").at(""https://repo1.maven.org/maven2/"");


    project = Export.of(BuildSystem.BAZEL, List.of(repo1, repo2), List.of(m1));
    // The resulting MODULE.bazel at the root will contain the following:
    // bazel_dep(name = "rules_jvm_external", version = "6.1")
    // 
    // maven = use_extension("@rules_jvm_external//:extensions.bzl", "maven")
    // maven.install(
    //     artifacts = ["my.other.module:other-module:1.1.1", "com.google.guava:guava:1.0"],
    //     fetch_sources = True,
    //     repositories = ["file:///Users/nicolas.couvrat/.m2/repository", "https://repo1.maven.org/maven2/"],
    // )
    // use_repo(maven, "maven")
    //
    // The resulting BUILD.bazel in myfirstmodule will contain the following:
    // java_library(
    //     name = "an.other.module",
    //     srcs = glob(["src/main/java/**/*.java"]),
    //     deps = ["@maven//:my_other_module_other_module", "@maven//:com_google_guava_guava"],
    // )
  }
}
```

## Credits

This library is inspired by a similar one found in Go's internal `packages` (`go/packages/packagetest`).

## License

   Copyright 2020 Nicolas Couvrat

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
