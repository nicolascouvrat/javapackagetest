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

## Available exporters

* `Kind.MAVEN`

## Example (using JUnit)

```java

package com.myorg.mytool;

import com.nikodoko.packagetest.Exported;
import com.nikodoko.packagetest.Module;
import com.nikodoko.packagetest.Export;
import com.nikodoko.packagetest.exporters.Kind;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import org.junit.Test;

public class MyToolTest {
  Exported project;

  @After
  public void cleanup() {
    project.cleanup();
  }

  @Test
  public void test() {
    // A first module
    Map<String, String> filesFirst = new HashMap<>();
    filesFirst.put("A.java", "package my.first.module;");
    filesFirst.put("b/B.java", "package my.first.module.b;");
    Module m1 = new Module("my.first.module", filesFirst);

    // A second module
    Map<String, String> filesSecond = new HashMap<>();
    filesSecond.put("C.java", "package my.second.module;");
    filesSecond.put("CTest.java", "package my.second.module;");
    Module m2 = new Module("my.second.module", filesSecond);

    List<Module> modules = new ArrayList<>();
    modules.add(m1);
    modules.add(m2);

    project = Export.of(Kind.MAVEN, modules);
    // You now how the following structure to work with
    // some_temporary_folder
    //  |
    //  - pom.xml
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
    //    - src/test/java/my/second/module
    //      |
    //      - CTest.java
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
