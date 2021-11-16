#!/usr/bin/env sh

set -e

cd $(dirname "$0")
if [ ! -d build ]; then
    mkdir build
fi

gcc \
    -O3 \
    -Wall \
    -Wpedantic \
    -Werror \
    -Wformat-security \
    -Wstack-protector \
    -ftrapv \
    -fPIE \
    -fstack-protector-all \
    -fstack-clash-protection \
    -D_FORTIFY_SOURCE=2 \
    src/pongccheatscli.c \
    -o build/pongccheatscli

sudo ./build/pongccheatscli $@
