#!/bin/bash

set -e

bazel run -- cmd --workspace-root testProjects/all-in-one --source-content-root src

pushd testProjects/all-in-one
bazel build //src/...
bazel test //src/...
popd
