#!/usr/bin/env bash
set -euo pipefail

# Build both x86_64 and arm64 release targets and create a universal binary
# Usage: ./package_universal.sh [--deploy]
#    --deploy: copy resulting universal binary to ../../bin/mint-mac-np

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "Adding required rust targets..."
rustup target add x86_64-apple-darwin aarch64-apple-darwin

echo "Building x86_64 release..."
cargo build --release --target x86_64-apple-darwin

echo "Building aarch64 (arm64) release..."
cargo build --release --target aarch64-apple-darwin

mkdir -p bin
X86_BIN="target/x86_64-apple-darwin/release/mint-mac-np"
ARM_BIN="target/aarch64-apple-darwin/release/mint-mac-np"
UNIVERSAL_BIN="bin/mint-mac-np"

if [ ! -f "$X86_BIN" ]; then
  echo "x86 build missing: $X86_BIN" >&2
  exit 1
fi
if [ ! -f "$ARM_BIN" ]; then
  echo "arm build missing: $ARM_BIN" >&2
  exit 1
fi

echo "Creating universal binary: $UNIVERSAL_BIN"
lipo -create -output "$UNIVERSAL_BIN" "$X86_BIN" "$ARM_BIN"
chmod +x "$UNIVERSAL_BIN"

echo "Created universal binary: $UNIVERSAL_BIN"

if [ "${1-}" = "--deploy" ]; then
  DEST_DIR="$(cd "$SCRIPT_DIR/../../" && pwd)/bin"
  mkdir -p "$DEST_DIR"
  cp "$UNIVERSAL_BIN" "$DEST_DIR/mint-mac-np"
  chmod +x "$DEST_DIR/mint-mac-np"
  echo "Deployed to $DEST_DIR/mint-mac-np"
fi
