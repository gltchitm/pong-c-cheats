#!/usr/bin/env sh

set -e

cargo build --release

OUTPUT_FILENAME_LINUX=libpongccheatstcpclient_native.so
OUTPUT_FILENAME_MACOS=libpongccheatstcpclient_native.dylib

if [ -f "./target/release/$OUTPUT_FILENAME_LINUX" ]; then
    filename=$OUTPUT_FILENAME_LINUX
elif [ -f "./target/release/$OUTPUT_FILENAME_MACOS" ]; then
    filename=$OUTPUT_FILENAME_MACOS
fi

mkdir -p ./build
cp ./target/release/$filename ./build/pongccheatstcpclient_native.node
