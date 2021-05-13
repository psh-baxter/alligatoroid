#!/usr/bin/env python3
import argparse
import os.path
import os
import subprocess
import tempfile
import contextlib
import base64

parser = argparse.ArgumentParser()
parser.add_argument("project")
args = parser.parse_args()

resources_java = [f"""
package com.zarbosoft.merman.{args.project};

import java.util.Base64;

public class Embedded {{"""]


def embed(name, path):
    with open(path, "rb") as source:
        data = source.read()
    resources_java[0] += f"""
    public static final byte[] {name} = Base64.getDecoder().decode("{base64.b64encode(data).decode("utf-8")}");
"""


@contextlib.contextmanager
def render_svg(path, size):
    with tempfile.NamedTemporaryFile(suffix='.png') as tf:
        subprocess.check_call(
            [
                "inkscape",
                path,
                "-o",
                tf.name,
                "-w",
                str(size),
                "-h",
                str(size),
            ]
        )
        yield tf.name

with render_svg('resources/icon.svg', 128) as r:
    embed("icon128", r)

with render_svg('resources/icon.svg', 64) as r:
    embed("icon64", r)

with render_svg('resources/icontiny.svg', 16) as r:
    embed("icon16", r)

resources_java[0] += "\n}\n"
resources_dir = args.project + "/target/sources/com/zarbosoft/merman/" + args.project
os.makedirs(resources_dir, exist_ok=True)
with open(resources_dir + "/Embedded.java", "wt") as dest:
    dest.write(resources_java[0])
