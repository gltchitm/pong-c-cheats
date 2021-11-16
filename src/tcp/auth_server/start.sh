#!/usr/bin/env sh

set -e

mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="com.github.gltchitm.pongccheatstcpauthserver.AppKt" -Dexec.args="$@"
