#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "== Verifica build Fisioarea =="
mvn -DskipTests clean package

echo "Build OK. Per creare pacchetti:"
echo "macOS Apple Silicon: ./release/build-macos-arm64-dmg.sh"
echo "Windows: powershell -ExecutionPolicy Bypass -File release/build-windows-exe.ps1"
