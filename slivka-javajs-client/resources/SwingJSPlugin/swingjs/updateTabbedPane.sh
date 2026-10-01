#!/bin/sh
set -eu

cd "$(dirname "$0")"
archive="$PWD/SwingJS-site.zip"
test -f "$archive" || { echo "Missing $archive" >&2; exit 1; }
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT

curl -fLsS \
  'https://raw.githubusercontent.com/koenvdheide/java2script/f505b9da1310292f8d08f4dc5d5c716a4afcae8f/sources/net.sf.j2s.java.core/dist/SwingJS-site.zip' \
  -o "$tmp/source.zip"

cd "$tmp"
set -- swingjs/j2s/swingjs/plaf/JSTabbedPaneUI.js
unzip -q source.zip "$@"
zip -X "$archive" "$@"
