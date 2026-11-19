#!/bin/sh
# Maven Wrapper (Unix) — downloads the Maven version declared in
# .mvn/wrapper/maven-wrapper.properties on first use, then delegates to it.
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
URL="$(grep '^distributionUrl=' "$DIR/.mvn/wrapper/maven-wrapper.properties" | cut -d= -f2- | tr -d '\r')"
NAME="$(basename "$URL" .zip | sed 's/-bin$//')"
BASE="${HOME}/.m2/wrapper/dists"
MVN="$BASE/$NAME/bin/mvn"
if [ ! -x "$MVN" ]; then
  mkdir -p "$BASE"
  echo "Downloading $URL" >&2
  if command -v curl >/dev/null 2>&1; then curl -fsSL "$URL" -o "$BASE/$NAME.zip"; else wget -q "$URL" -O "$BASE/$NAME.zip"; fi
  (cd "$BASE" && unzip -q -o "$NAME.zip" && rm -f "$NAME.zip")
  chmod +x "$MVN"
fi
exec "$MVN" -f "$DIR/pom.xml" "$@"
