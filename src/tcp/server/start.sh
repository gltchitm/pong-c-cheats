#!/usr/bin/env sh

set -e

pip3 install -r ./requirements.txt
python3 ./src/server.py
