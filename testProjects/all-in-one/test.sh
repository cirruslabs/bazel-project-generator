#!/bin/bash

set -e

./../../bazel-bin/cmd --source-content-root src

bazel build //src/...
bazel test //src/...
