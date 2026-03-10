#!/usr/bin/env bash
set -euo pipefail

# Run the NowPlaying Spring Boot application on macOS for development
# Usage: ./run_java_mac.sh [project-root]
# If project-root omitted, script will assume two levels up is the project root.

PROJECT_ROOT="${1:-$(cd "$(dirname "$0")/../.." && pwd)}"
MAIN_CLASS="com.widdit.nowplaying.NowPlayingApplication"

CLASSES_DIR="$PROJECT_ROOT/target/classes"
if [ -d "$CLASSES_DIR" ]; then
  echo "Running $MAIN_CLASS from $CLASSES_DIR"
  java -cp "$CLASSES_DIR:$(echo $PROJECT_ROOT/lib/* 2>/dev/null | tr ' ' ':')" "$MAIN_CLASS"
  exit 0
fi

if [ -f "$PROJECT_ROOT/pom.xml" ]; then
  echo "No classes found; invoking Maven to run the main class"
  (cd "$PROJECT_ROOT" && mvn -DskipTests -Dexec.mainClass="$MAIN_CLASS" -q exec:java)
  exit 0
fi

echo "Cannot find compiled classes or pom.xml in $PROJECT_ROOT. Build the project first." >&2
exit 1
