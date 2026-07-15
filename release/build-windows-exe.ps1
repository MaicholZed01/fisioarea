$ErrorActionPreference = "Stop"

$AppName = "Fisioarea"
$AppVersion = "1.0.0"
$MainClass = "com.fisioarea.app.Launcher"
$RootDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$InputDir = Join-Path $RootDir "target\package-input"
$OutputDir = Join-Path $RootDir "target\release\windows"
$IconPath = Join-Path $RootDir "src\main\resources\com\fisioarea\assets\fisioarea-icon.ico"

Write-Host "== Fisioarea Windows EXE =="

if (-not $IsWindows) {
    Write-Error "L'EXE Windows deve essere creato da Windows."
}

if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    Write-Error "Maven non installato o non presente nel PATH."
}

if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    Write-Error "Java/JDK non installato o non presente nel PATH."
}

if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
    Write-Error "jpackage non trovato. Installa un JDK completo, non solo JRE."
}

if (-not (Test-Path $IconPath)) {
    Write-Error "Icona Windows non trovata: $IconPath"
}

Write-Host "Java:" -NoNewline
java -version
Write-Host "jpackage: $(jpackage --version)"
Write-Host "Maven: $((mvn -version | Select-Object -First 1))"

if (Test-Path $InputDir) { Remove-Item $InputDir -Recurse -Force }
if (Test-Path $OutputDir) { Remove-Item $OutputDir -Recurse -Force }
New-Item -ItemType Directory -Force -Path $InputDir | Out-Null
New-Item -ItemType Directory -Force -Path $OutputDir | Out-Null

Write-Host "== Build Maven =="
mvn -DskipTests clean package dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=$InputDir"

$Jar = Get-ChildItem -Path (Join-Path $RootDir "target") -Filter "fisioarea-mvc-*.jar" | Select-Object -First 1
if (-not $Jar) {
    Write-Error "Jar applicazione non trovato in target/."
}

Copy-Item $Jar.FullName (Join-Path $InputDir "Fisioarea.jar") -Force

Write-Host "== Creazione installer EXE =="
jpackage `
  --type exe `
  --name $AppName `
  --app-version $AppVersion `
  --vendor "Fisioarea" `
  --description "Gestionale offline per studio di fisioterapia" `
  --dest $OutputDir `
  --input $InputDir `
  --main-jar "Fisioarea.jar" `
  --main-class $MainClass `
  --icon $IconPath `
  --win-menu `
  --win-shortcut `
  --win-dir-chooser `
  --win-per-user-install `
  --java-options "--enable-native-access=javafx.graphics"

Write-Host ""
Write-Host "EXE creato in: $OutputDir"
Get-ChildItem $OutputDir
