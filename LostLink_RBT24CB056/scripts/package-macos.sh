#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
destination="${1:-target/macos}"
if [ -e "$destination/LostLink.app" ]; then
  printf '%s\n' "An app already exists at $destination/LostLink.app. Pass a fresh output folder, e.g. ./scripts/package-macos.sh target/macos-new"
  exit 1
fi
./mvnw package
mkdir -p target/package-input
cp target/lostlink-1.0.0.jar target/package-input/
jpackage --type app-image --name LostLink --app-version 1.0.0 \
  --input target/package-input --main-jar lostlink-1.0.0.jar \
  --main-class com.lostlink.App --dest "$destination" \
  --mac-package-identifier com.lostlink.desktop \
  --java-options '--enable-native-access=ALL-UNNAMED'
