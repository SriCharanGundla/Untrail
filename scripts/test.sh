#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p core-tests/build
javac -d core-tests/build app/src/main/java/app/untrail/{Cleaner,DraftPolicy}.java core-tests/{CleanerTest,DraftPolicyTest}.java
java -cp core-tests/build CleanerTest
java -cp core-tests/build DraftPolicyTest
