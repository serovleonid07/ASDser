#!/bin/sh
# Builds FoxAndGeese.jar from the sources (needs JDK 17 or newer).
cd "$(dirname "$0")"
rm -rf build && mkdir -p build/classes
javac --release 17 -encoding UTF-8 -d build/classes src/*.java || exit 1
cp -r src/images build/classes/images
jar --create --file FoxAndGeese.jar --main-class Main -C build/classes .
rm -rf build
echo "Done: FoxAndGeese.jar (run: java -jar FoxAndGeese.jar)"
