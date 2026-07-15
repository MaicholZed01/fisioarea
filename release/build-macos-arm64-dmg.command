#!/usr/bin/env bash
cd "$(dirname "$0")/.."
chmod +x release/build-macos-arm64-dmg.sh
./release/build-macos-arm64-dmg.sh
