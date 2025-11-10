#!/usr/bin/env bash

# This script builds the schema, then tears down and rebuilds
# the local podman-compose development environment.
#
# It is intended to be run from the root of the project.

set -e

# Path to compose file
COMPOSE_FILE="./podman/podman-compose.yml"

echo "--- Building the component with schemas ---"
bash ./hack/build-with-schemas.sh

echo "--- Switching to podman folder ---"
pushd podman

echo "--- Stopping existing podman-compose ---"
podman-compose down

echo "--- Starting podman-compose ---"
podman-compose up --build

echo "--- Local podman-compose is now running ---"