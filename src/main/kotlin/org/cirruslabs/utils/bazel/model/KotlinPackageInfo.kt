package org.cirruslabs.utils.bazel.model

class KotlinPackageInfo(
  fullyQualifiedName: String,
  targetPath: String
) : PackageInfo(fullyQualifiedName, targetPath, "kt") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    val deps = directPackageDependencies.mapNotNull {
      packageRegistry.findInfo(it)?.fullTargetLocation
    }.sorted().joinToString(separator = "\n") { "\"$it\"," }
    return """# GENERATED
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library")

kt_jvm_library(
  name = "$targetName",
  srcs = glob(["*.kt"]),
  visibility = ["//visibility:public"],
  deps = [
${deps.prependIndent("    ")}
  ],
)"""
  }
}
