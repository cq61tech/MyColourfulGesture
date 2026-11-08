#!/usr/bin/env bash
# Creates a release keystore and stores it in this repository's GitHub secrets.
# Run it inside the repository, logged in with `gh auth login`.
set -e

command -v keytool >/dev/null || { echo "keytool missing. In Termux: pkg install openjdk-17"; exit 1; }
command -v gh >/dev/null || { echo "gh missing. In Termux: pkg install gh"; exit 1; }

OUT="${1:-$HOME/mcg-release.jks}"
if [ -e "$OUT" ]; then
  echo "$OUT already exists, refusing to overwrite it"
  exit 1
fi

PASS=$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)
ALIAS=mcg

keytool -genkeypair -keystore "$OUT" -alias "$ALIAS" -keyalg RSA -keysize 4096 \
  -validity 36500 -storepass "$PASS" -keypass "$PASS" \
  -dname "CN=MyColourfulGesture, O=cq61tech"

base64 -w0 "$OUT" | gh secret set KEYSTORE_BASE64
printf %s "$PASS" | gh secret set KEYSTORE_PASSWORD
printf %s "$PASS" | gh secret set KEY_PASSWORD
printf %s "$ALIAS" | gh secret set KEY_ALIAS

echo
echo "Done. Secrets were written to GitHub."
echo "Keystore: $OUT"
echo "Password: $PASS"
echo
echo "BACK THESE TWO UP somewhere safe and offline. If you lose them,"
echo "users will not be able to update the app without reinstalling it."
