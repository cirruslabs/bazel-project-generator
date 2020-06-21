package org.cirruslabs.utils.bazel.model

import java.util.*

class PackageInfo(val fullyQualifiedName: String, val targetPath: String, val targetName: String) {
  private val directPackageDependencies: MutableSet<String> = TreeSet<String>()

  val fullTargetLocation
    get() = "$targetPath:targetName"

  fun addDirectPackageDependencies(importedPackages: Set<String>) {
    directPackageDependencies.addAll(importedPackages)
  }

  fun generateTarget(packageRegistry: PackageRegistry): String {
    val deps = directPackageDependencies.mapNotNull {
      packageRegistry.findInfo(it)?.fullTargetLocation
    }.joinToString(separator = "\n") { "\"$it\"," }
    return """
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
