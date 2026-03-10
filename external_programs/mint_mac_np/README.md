This directory contains a copy of the `mint-mac-np` Rust project and helper scripts to build and run.

Files
- `package_universal.sh` — build x86_64 and arm64 release binaries and create a universal `bin/mint-mac-np` using `lipo`.
    - Usage: `./package_universal.sh`
    - To also copy the universal binary to the parent service `bin` directory, run `./package_universal.sh --deploy`.
- `package_dmg_mac.sh` — build and package `now-playing-service` into macOS DMG(s), while trying to output `x86_64`, `arm64`, and `universal` labels when runtime/toolchain support exists.
    - Usage: `./package_dmg_mac.sh`
    - Artifacts: `../../Outputs/NowPlaying/NowPlaying-*.dmg` and helper binaries (`mint-mac-np-*`).
- `run_java_mac.sh` — helper script to run the Java Spring Boot application on macOS during development.
    - Usage: `./run_java_mac.sh [project-root]`

Prerequisites
- macOS with `lipo` (Xcode command line tools)
- Rust toolchain with `cargo` and `rustup`
- For `run_java_mac.sh`: JDK and Maven (or pre-built `target/classes`)

Notes
- The packaging script builds both targets (`x86_64-apple-darwin` and `aarch64-apple-darwin`) and uses `lipo` to produce a universal binary under `bin/`.
- The `--deploy` option copies the universal binary to the repository's service `bin` directory (if available).
- DMG packaging requires a working JDK/JRE (`java`) and `jpackage`; cross-arch DMG output depends on whether alternate-arch Java is runnable through `arch`.
