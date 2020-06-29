package org.cirruslabs.utils.bazel.model.proto

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class JavaProtoInfo(
  fullyQualifiedName: String,
  targetPath: String
) : PackageInfo(fullyQualifiedName, targetPath, "java_proto") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    return """# GENERATED
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")

proto_library(
  name = "proto",
  srcs = glob(["*.proto"]),
)

java_proto_library(
  name = "$targetName",
  deps = [":proto"],
)"""
  }
}
