#!/bin/bash

function error {
    echo "Error: $@" >&2
}

function error_exit {
    error "$@"
    exit 1
}

VERSION=$(grep "version in ThisBuild" build.sbt | sed 's/.*"\(.*\)"/\1/')

APPSTORE="../TestAppStore/Multi/lib"
if [ -n "$1" ]; then
    APPSTORE=$1
fi

[ -d "${APPSTORE}" ] || error_exit "App store does not exist or is not a directory: ${APPSTORE}"

echo "Installing Serve App assemblies of version ${VERSION}..."
for package in app datamodel protocol; do
  cp ${package}/target/scala-2.10/multi-${package}-assembly-${VERSION}.jar "${APPSTORE}" || exit 1
done
echo "Done."
