#!/usr/bin/env sh

set -e

npm --prefix client ci
npm --prefix client run build
GIN_MODE=release go run .
