#!/usr/bin/env sh

set -e

cargo build --release
sudo ./target/release/pongccheatsd
