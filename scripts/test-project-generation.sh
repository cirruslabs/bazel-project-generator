#!/bin/bash

set -e

for testProjectDir in testProjects/*/
do
  pushd $testProjectDir
  ./test.sh
  popd
done
