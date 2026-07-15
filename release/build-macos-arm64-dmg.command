#!/usr/bin/env bash
DIR="$(cd "$(dirname "$0")" && pwd)"
"$DIR/build-macos-arm64-dmg.sh"
echo ""
echo "Premi Invio per chiudere..."
read -r _
