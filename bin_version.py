#!/usr/bin/env python3
import argparse
import subprocess
import re

if subprocess.call(['git', 'diff-index', '--quiet', 'HEAD']) != 0:
    raise RuntimeError('Working directory must be clean.')
parser = argparse.ArgumentParser()
parser.add_argument('version')
args = parser.parse_args()
if not re.match('\\d+\\.\\d+\\.\\d+', args.version):
    args.error('version must be in the format N.N.N')
subprocess.check_call([
    'mvn',
    'versions:set',
    '-DnewVersion={}'.format(args.version),
    '-DgenerateBackupPoms=false',
])
subprocess.check_call([
    'sed',
    '-e', '"s/\\(Version \\|merman-\\|v\\)[[:digit:]]\\+\\.[[:digit:]]\\+\\.[[:digit:]]\\+/\\1{}/g"'.format(  # noqa
        args.version
    ),
    '-i',
    'readme.md',
])
subprocess.call([
    'git',
    'commit',
    '-a',
    '-m', 'VERSION {}'.format(args.version),
])
subprocess.check_call([
    'git',
    'tag',
    '-a', 'v{}'.format(args.version),
    '-m', 'v{}'.format(args.version),
    '-f',
])
subprocess.check_call([
    'git',
    'push',
])
subprocess.check_call([
    'git',
    'push',
    '--tags',
    '-f',
])
