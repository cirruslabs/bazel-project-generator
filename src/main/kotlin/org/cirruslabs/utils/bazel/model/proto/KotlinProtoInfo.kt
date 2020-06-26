package org.cirruslabs.utils.bazel.model.proto

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class KotlinProtoInfo(
  fullyQualifiedName: String,
  targetPath: String
) : PackageInfo(fullyQualifiedName, targetPath, "kt_proto") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    return """# GENERATED
load("@rules_proto//proto:defs.bzl", "proto_library")
load("@io_grpc_grpc_java//:java_grpc_library.bzl", "java_grpc_library")
load("@com_github_grpc_grpc_kotlin//:kt_jvm_grpc.bzl", "kt_jvm_grpc_library")

proto_library(
  name = "proto",
  srcs = glob(["*.proto"]),
)

java_proto_library(
  name = "java_proto",
  deps = [":proto"],
)

kt_jvm_grpc_library(
  name = "$targetName",
  srcs = [":proto"],
  deps = [":java_proto"],
  visibility = ["//visibility:public"],
)"""
  }
}
