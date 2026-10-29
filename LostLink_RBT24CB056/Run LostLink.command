#!/bin/zsh
set -eu
cd -- "${0:A:h}"
if [ ! -f target/lostlink-1.0.0.jar ]; then
  ./mvnw package
fi
exec java --enable-native-access=ALL-UNNAMED -Xdock:name=LostLink -jar target/lostlink-1.0.0.jar
