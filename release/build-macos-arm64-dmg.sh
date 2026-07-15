#!/usr/bin/env bash
set -euo pipefail

APP_NAME="Fisioarea"
APP_VERSION="1.0.0"
APP_ID="com.fisioarea.app"
MAIN_CLASS="com.fisioarea.app.Launcher"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
INPUT_DIR="$ROOT_DIR/target/package-input"
OUTPUT_DIR="$ROOT_DIR/target/release/macos-arm64"
ICON_PATH="$ROOT_DIR/src/main/resources/com/fisioarea/assets/fisioarea-icon.icns"

cd "$ROOT_DIR"

echo "== Fisioarea macOS Apple Silicon DMG =="

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "Errore: il DMG macOS deve essere creato da macOS."
  exit 1
fi

if [[ "$(uname -m)" != "arm64" ]]; then
  echo "Errore: per chip M serve un Mac Apple Silicon, architettura arm64."
  echo "Architettura rilevata: $(uname -m)"
  exit 1
fi

command -v mvn >/dev/null 2>&1 || { echo "Errore: Maven non installato."; exit 1; }
command -v java >/dev/null 2>&1 || { echo "Errore: Java/JDK non installato."; exit 1; }
command -v jpackage >/dev/null 2>&1 || { echo "Errore: jpackage non trovato. Installa un JDK completo, non solo JRE."; exit 1; }

if [[ ! -f "$ICON_PATH" ]]; then
  echo "Errore: icona macOS non trovata: $ICON_PATH"
  exit 1
fi

echo "Java: $(java -version 2>&1 | head -n 1)"
echo "jpackage: $(jpackage --version)"
echo "Maven: $(mvn -version | head -n 1)"

rm -rf "$INPUT_DIR" "$OUTPUT_DIR"
mkdir -p "$INPUT_DIR" "$OUTPUT_DIR"

echo "== Build Maven =="
mvn -DskipTests clean package dependency:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory="$INPUT_DIR"

APP_JAR="$(ls "$ROOT_DIR"/target/fisioarea-mvc-*.jar | head -n 1)"
if [[ -z "$APP_JAR" || ! -f "$APP_JAR" ]]; then
  echo "Errore: jar applicazione non trovato in target/."
  exit 1
fi

cp "$APP_JAR" "$INPUT_DIR/Fisioarea.jar"

echo "== Creazione DMG =="
jpackage \
  --type dmg \
  --name "$APP_NAME" \
  --app-version "$APP_VERSION" \
  --vendor "Fisioarea" \
  --description "Gestionale offline per studio di fisioterapia" \
  --dest "$OUTPUT_DIR" \
  --input "$INPUT_DIR" \
  --main-jar "Fisioarea.jar" \
  --main-class "$MAIN_CLASS" \
  --icon "$ICON_PATH" \
  --mac-package-name "$APP_NAME" \
  --mac-package-identifier "$APP_ID" \
  --java-options "-Dapple.awt.application.name=$APP_NAME" \
  --java-options "--enable-native-access=javafx.graphics"

echo ""
echo "DMG creato in: $OUTPUT_DIR"
ls -lh "$OUTPUT_DIR" || true
