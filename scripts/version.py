#!/usr/bin/env python3
"""Validate release versions and derive an increasing Android versionCode."""
import re
import sys


def version_code(version):
    if not re.fullmatch(r"(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)\.(0|[1-9][0-9]*)", version):
        raise ValueError("Version must be X.Y.Z without leading zeros or a v prefix")
    major, minor, patch = map(int, version.split("."))
    code = major * 1_000_000 + minor * 1_000 + patch
    if minor > 999 or patch > 999 or not 1 <= code <= 2_100_000_000:
        raise ValueError("Version exceeds Android limits; minor and patch must be <= 999")
    return code


if __name__ == "__main__":
    try:
        print(version_code(sys.argv[1]))
    except (ValueError, IndexError) as error:
        sys.exit(str(error))
