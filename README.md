# Terminala

Terminala is a standalone terminal emulator written in Java, inspired by Tilix, and built on top of **JetBrains JediTerm**.

The main goal of this project is to provide a real, embeddable terminal application implemented in Java, independent from any IDE, with a simple and pragmatic architecture.

This project exists because there are very few standalone terminal emulators written in Java, and most existing solutions are either demos, IDE-bound, or abandoned.

---

## Features

- Standalone terminal emulator written in Java
- Uses JetBrains **JediTerm** as the terminal widget
- Multi-panel layout (Tilix-like behavior)
- Real shell execution (PTY-based)
- Desktop application (not an IDE plugin)

---

## Why Java?

Implementing a real terminal emulator in Java is non-trivial:

- No native PTY support
- Signal handling (resize, redraw, etc.)
- Performance and repaint complexity
- Scrollback and input handling

JediTerm solves most of these problems and is the same terminal engine used by IntelliJ-based IDEs.  
Terminala builds on top of that work to provide a **standalone** application.

---

## JediTerm and Dependencies

This project **includes prebuilt JARs of JediTerm and its required dependencies** directly in the repository.

They were obtained by cloning the official JediTerm repository and running:

```bash
./gradlew build
./gradlew lib
```

The generated JAR files are then used as-is by Terminala.

This approach is intentional:

No dependency resolution required

No Gradle/Maven setup needed to run

Simplifies usage and distribution

While this is not the ideal dependency model, it is practical and fully compliant with the licenses involved.

## Licensing Terminala

Terminala’s own source code is released under the MIT License.
See the LICENSE file for details.

## JediTerm

JediTerm is developed by JetBrains and is licensed under the Apache License 2.0.

Copyright © JetBrains

## License: Apache License 2.0

The full license text is included in LICENSE-JEDITERM

This project does not claim ownership of JediTerm or any of its dependencies.

## Releases

The release/ directory contains a ZIP archive with a ready-to-run version of Terminala, including:

All required JAR files

No build step required

No external dependencies

## Contributions

This is currently a personal project and does not accept external contributions at this time.

## Disclaimer

Terminala is an independent project and is not affiliated with or endorsed by JetBrains.
JediTerm is used in accordance with its Apache 2.0 license.

## Status

Active development.
