#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="${ANDROID_SDK_ROOT:-$HOME/Library/Android/sdk}"
BT="$SDK/build-tools/36.0.0"
PLATFORM="$SDK/platforms/android-36.1/android.jar"
OUT="$PWD/app/build/local"
mkdir -p "$OUT/classes" "$OUT/generated" "$OUT/dex"
"$BT/aapt2" compile --dir app/src/main/res -o "$OUT/resources.zip"
python3 - "$OUT/AndroidManifest.xml" <<'PY'
import sys
s=open('app/src/main/AndroidManifest.xml').read().replace('<manifest xmlns:android=', '<manifest package="app.untrail" android:versionCode="5004" android:versionName="0.5.4" xmlns:android=')
s=s.replace(' <queries>', '<uses-sdk android:minSdkVersion="31" android:targetSdkVersion="36"/>\n <queries>')
open(sys.argv[1],'w').write(s)
PY
"$BT/aapt2" link -o "$OUT/base.apk" -I "$PLATFORM" --manifest "$OUT/AndroidManifest.xml" --java "$OUT/generated" "$OUT/resources.zip"
find app/src/main/java "$OUT/generated" -name '*.java' > "$OUT/sources.txt"
javac -source 17 -target 17 -classpath "$PLATFORM" -d "$OUT/classes" @"$OUT/sources.txt"
jar cf "$OUT/classes.jar" -C "$OUT/classes" .
"$BT/d8" --lib "$PLATFORM" --min-api 31 --output "$OUT/dex" "$OUT/classes.jar"
cp "$OUT/base.apk" "$OUT/unsigned.apk"
(cd "$OUT/dex" && zip -q "$OUT/unsigned.apk" classes*.dex)
"$BT/zipalign" -f 4 "$OUT/unsigned.apk" "$OUT/aligned.apk"
if [ ! -f "$OUT/debug.jks" ]; then
 keytool -genkeypair -keystore "$OUT/debug.jks" -storepass android -keypass android -alias androiddebugkey -dname "CN=Untrail Debug" -keyalg RSA -validity 10000 -noprompt
fi
"$BT/apksigner" sign --ks "$OUT/debug.jks" --ks-pass pass:android --out "$OUT/untrail-debug.apk" "$OUT/aligned.apk"
"$BT/apksigner" verify "$OUT/untrail-debug.apk"
printf 'APK: %s\n' "$OUT/untrail-debug.apk"
