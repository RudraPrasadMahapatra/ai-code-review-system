#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

set -euf

[ "${MVNW_VERBOSE-}" = "true" ] && set -x

BASE_DIR=$(cd "$(dirname "$0")" && pwd -P)

MAVEN_PROJECTBASEDIR=${MAVEN_PROJECTBASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR

WRAPPER_DIR="$MAVEN_PROJECTBASEDIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
PROPERTIES_FILE="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$WRAPPER_JAR" ]; then
  if [ ! -f "$PROPERTIES_FILE" ]; then
    echo "Missing $PROPERTIES_FILE" >&2
    exit 1
  fi

  WRAPPER_URL=$(grep -E '^wrapperUrl=' "$PROPERTIES_FILE" | cut -d= -f2- || true)
  if [ -z "${WRAPPER_URL:-}" ]; then
    WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
  fi

  mkdir -p "$WRAPPER_DIR"
  echo "Downloading Maven Wrapper from $WRAPPER_URL"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL -o "$WRAPPER_JAR" "$WRAPPER_URL"
  elif command -v wget >/dev/null 2>&1; then
    wget -q -O "$WRAPPER_JAR" "$WRAPPER_URL"
  else
    echo "Neither curl nor wget is available to download Maven Wrapper." >&2
    exit 1
  fi
fi

JAVA_EXEC="${JAVA_HOME-}/bin/java"
if [ ! -x "$JAVA_EXEC" ]; then
  JAVA_EXEC="$(command -v java || true)"
fi
if [ -z "$JAVA_EXEC" ]; then
  echo "Java not found. Set JAVA_HOME or ensure 'java' is on PATH." >&2
  exit 1
fi

exec "$JAVA_EXEC" \
  -classpath "$WRAPPER_JAR" \
  -Dmaven.multiModuleProjectDirectory="$MAVEN_PROJECTBASEDIR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"

