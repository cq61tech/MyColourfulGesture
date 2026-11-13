#!/usr/bin/env bash
set -e

command -v keytool >/dev/null || { echo "keytool not found (pkg install openjdk-17)"; exit 1; }
command -v gh >/dev/null || { echo "gh not found (pkg install gh)"; exit 1; }

OUT="${1:-$HOME/mcg-release.jks}"
[ -e "$OUT" ] && { echo "$OUT exists, not overwriting"; exit 1; }

PASS=$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)

keytool -genkeypair -keystore "$OUT" -alias mcg -keyalg RSA -keysize 4096 \
  -validity 36500 -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=MyColourfulGesture, O=cq61tech"

base64 -w0 "$OUT" | gh secret set KEYSTORE_BASE64
printf %s "$PASS" | gh secret set KEYSTORE_PASSWORD
printf %s "$PASS" | gh secret set KEY_PASSWORD
printf %s mcg | gh secret set KEY_ALIAS

echo "keystore: $OUT"
echo "password: $PASS"
echo "keep both, you cannot ship updates without them"
