#!/usr/bin/env python3
import argparse
import os.path
import os
import subprocess
import tempfile

parser = argparse.ArgumentParser()
parser.add_argument("project")
args = parser.parse_args()

with tempfile.NamedTemporaryFile(suffix='.png') as tf:
    size = 48
    subprocess.check_call(
        [
            "inkscape",
            "resources/icon.svg",
            "-o",
            tf.name,
            "-w",
            str(size),
            "-h",
            str(size),
        ]
    )
    with open(tf.name, "rb") as source:
        iconbytes = source.read()

resources_java = f"""
package com.zarbosoft.merman.{args.project};

public class Embedded {{
    public static final byte[] icon48 = new byte[] {{
        {", ".join("(byte)" + str(int(b)) for b in iconbytes)}
    }};
}}
"""
resources_dir = args.project + "/target/sources/com/zarbosoft/merman/" + args.project
os.makedirs(resources_dir, exist_ok=True)
with open(resources_dir + "/Embedded.java", "wt") as dest:
    dest.write(resources_java)
