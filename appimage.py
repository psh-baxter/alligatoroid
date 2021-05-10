#!/usr/bin/env python3
import argparse
import urllib.request
import shutil
import os.path
import os
import subprocess

parser = argparse.ArgumentParser()
parser.add_argument("project")
args = parser.parse_args()

sourcedir = args.project + "/target/image"

ait = "appimagetool-x86_64.AppImage"
if not os.path.exists(ait):
    with urllib.request.urlopen(
        "https://github.com/AppImage/AppImageKit/releases/download/12/appimagetool-x86_64.AppImage"
    ) as source:
        with open(ait, "wb") as dest:
            shutil.copyfileobj(source, dest)
    os.chmod(ait, 0o744)

apprun = """#!/bin/sh
SELF_DIR="$(dirname "$(readlink -f "$0")")"
exec "$SELF_DIR/bin/java" \
    -p modules \
    -m com.zarbosoft.merman.jfxeditor1/com.zarbosoft.merman.jfxeditor1.Main
"""
with open(sourcedir + "/AppRun", "wt") as dest:
    dest.write(apprun)

desktop = """[Desktop Entry]
Type=Application
Name=merman1
Icon=icon
"""
with open(sourcedir + "/merman1.desktop", "wt") as dest:
    dest.write(desktop)

shutil.copyfile('resources/icon.svg', sourcedir + "/icon.svg")

subprocess.check_call(['./' + ait, sourcedir])
