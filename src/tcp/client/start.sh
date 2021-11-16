#!/usr/bin/env sh

set -e

NATIVE_PATH=./client_service/native
cargo build --manifest-path=$NATIVE_PATH/Cargo.toml --release

OUTPUT_FILENAME_LINUX=libpongccheatstcpclient_native.so
OUTPUT_FILENAME_MACOS=libpongccheatstcpclient_native.dylib

if [ -f "$NATIVE_PATH/target/release/$OUTPUT_FILENAME_LINUX" ]; then
    filename=$OUTPUT_FILENAME_LINUX
elif [ -f "$NATIVE_PATH/target/release/$OUTPUT_FILENAME_MACOS" ]; then
    filename=$OUTPUT_FILENAME_MACOS
else
    echo "unsupported system" 1>&2
    exit 1
fi

mkdir -p $NATIVE_PATH/build
cp $NATIVE_PATH/target/release/$filename $NATIVE_PATH/build/pongccheatstcpclient_native.node

npm ci
MESA_GLSL_CACHE_DISABLE=true npm run start
