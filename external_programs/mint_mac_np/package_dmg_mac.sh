#!/usr/bin/env bash
set -euo pipefail

# Package now-playing-service as macOS DMG(s), and try to output multiple arch variants when possible.
# Outputs are written to: <repo>/Outputs/NowPlaying
#
# Behavior:
# 1) Build mint-mac-np x86_64 + arm64 + universal and deploy universal to <repo>/bin/mint-mac-np
# 2) Build Spring Boot jar via Maven
# 3) Use jpackage to create DMG for host arch
# 4) Try cross-arch packaging via `arch -x86_64` / `arch -arm64` when possible
#
# Note: true universal DMG depends on the Java runtime used by jpackage.

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
OUTPUT_DIR="$ROOT_DIR/Outputs/NowPlaying"
BUILD_TMP="$OUTPUT_DIR/_build"
APP_NAME="NowPlaying"
MAIN_CLASS="org.springframework.boot.loader.JarLauncher"

mkdir -p "$OUTPUT_DIR" "$BUILD_TMP"

echo "[1/4] Building mint-mac-np (x86_64 + arm64 + universal)..."
"$SCRIPT_DIR/package_universal.sh" --deploy
cp -f "$SCRIPT_DIR/target/x86_64-apple-darwin/release/mint-mac-np" "$OUTPUT_DIR/mint-mac-np-x86_64"
cp -f "$SCRIPT_DIR/target/aarch64-apple-darwin/release/mint-mac-np" "$OUTPUT_DIR/mint-mac-np-arm64"
cp -f "$SCRIPT_DIR/bin/mint-mac-np" "$OUTPUT_DIR/mint-mac-np-universal"
chmod +x "$OUTPUT_DIR/mint-mac-np-x86_64" "$OUTPUT_DIR/mint-mac-np-arm64" "$OUTPUT_DIR/mint-mac-np-universal"

echo "[2/4] Building Spring Boot jar..."
(cd "$ROOT_DIR" && mvn -DskipTests package)

JAR_PATH="$(ls -1 "$ROOT_DIR"/target/now-playing-*.jar 2>/dev/null | grep -v 'original' | head -n 1 || true)"
if [[ -z "$JAR_PATH" ]]; then
  echo "ERROR: Cannot find packaged jar under $ROOT_DIR/target (expected now-playing-*.jar)" >&2
  exit 1
fi
JAR_NAME="$(basename "$JAR_PATH")"

echo "[3/4] Preparing jpackage input..."
PKG_INPUT="$BUILD_TMP/input"
rm -rf "$PKG_INPUT"
mkdir -p "$PKG_INPUT"
cp -f "$JAR_PATH" "$PKG_INPUT/$JAR_NAME"

# jpackage requires numeric version and the first number must be > 0
APP_VERSION="1.0.1"

package_with_cmd() {
  local label="$1"
  local java_cmd="$2"

  local out_dir="$BUILD_TMP/$label"
  rm -rf "$out_dir"
  mkdir -p "$out_dir"

  echo "[4/4][$label] Running jpackage..."
  if ! eval "$java_cmd -version >/dev/null 2>&1"; then
    echo "[$label] SKIP: Java runtime not available for command: $java_cmd"
    return 0
  fi

  local jpackage_cmd
  jpackage_cmd="${java_cmd% java} jpackage"
  if ! eval "$jpackage_cmd --version >/dev/null 2>&1"; then
    # fallback to plain jpackage
    jpackage_cmd="jpackage"
  fi

  set +e
  eval "$jpackage_cmd --type dmg --name '$APP_NAME' --dest '$out_dir' --input '$PKG_INPUT' --main-jar '$JAR_NAME' --main-class '$MAIN_CLASS' --app-version '$APP_VERSION' --vendor 'Widdit'"
  local code=$?
  set -e
  if [[ $code -ne 0 ]]; then
    echo "[$label] jpackage failed (exit=$code)"
    return 0
  fi

  local dmg
  dmg="$(find "$out_dir" -maxdepth 1 -type f -name '*.dmg' | head -n 1 || true)"
  if [[ -z "$dmg" ]]; then
    echo "[$label] No dmg produced"
    return 0
  fi

  cp -f "$dmg" "$OUTPUT_DIR/${APP_NAME}-${label}.dmg"
  echo "[$label] Output: $OUTPUT_DIR/${APP_NAME}-${label}.dmg"
}

HOST_ARCH="$(uname -m)"
echo "Host arch: $HOST_ARCH"

# host dmg
if [[ "$HOST_ARCH" == "arm64" ]]; then
  package_with_cmd "arm64" "java"
  package_with_cmd "x86_64" "arch -x86_64 java"
elif [[ "$HOST_ARCH" == "x86_64" ]]; then
  package_with_cmd "x86_64" "java"
  package_with_cmd "arm64" "arch -arm64 java"
else
  package_with_cmd "host" "java"
fi

# Attempt "universal" label when java binary itself is universal
JAVA_BIN="$(command -v java || true)"
if [[ -n "$JAVA_BIN" ]]; then
  JAVA_FILE_DESC="$(file -L "$JAVA_BIN" || true)"
  if echo "$JAVA_FILE_DESC" | grep -qi 'universal'; then
    package_with_cmd "universal" "java"
  else
    echo "[universal] SKIP: current java launcher is not reported as universal"
  fi
fi

echo "Done. Artifacts directory: $OUTPUT_DIR"
ls -1 "$OUTPUT_DIR" | sed 's/^/ - /'
