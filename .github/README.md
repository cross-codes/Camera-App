<div align="center">
<h1>📷 Camera App </h1>

A camera app to take and save images from any connected webcams,
written in Java, and built with Maven.

Current version: 2.0

</div>

---

# Build instructions

This app is purely made in Java, and is hence universally compatible with any
x86-64 system.
Before you build the app, there are some necessary manual changes to the
contents of the file `📁src/main/java/com/github/cross/App.java`,

```java
package com.github.cross;

public class App {
  public static void main(String[] args) {
    WebCamHandler.imageLocation = "/home/cross/Pictures/Camera/"; // 💀
    WebCamHandler.main(args);
  }
}
```

The code marked with a skull needs to be changed. Change the `imageLocation` to
the location where you would like pictures to be stored. Ensure it ends with `/`
(or `\` for Windows).

Once changed, in the root folder containing `pom.xml` run the following

```bash
mvn clean package
```

Then an uber jar will be created in `📁targets/` called `Camera-App-2.0-FINAL.jar`.
This can be renamed and/or moved, and run with the `java -jar <JAR_FILE>` command.

---

# Development

For development, instead of continuously packaging, you can execute the command chain:

```bash
mvn clean install # Neccessary only if there are changes to the pom.xml file
mvn exec:java -Dexec.mainClass="com.github.cross.App"
```

---

Project started on: 07/06/2024

(v1.0) First functional version completed on: 01/07/2024

(v2.0) Bug fixes and styles added on: 12/07/2024
