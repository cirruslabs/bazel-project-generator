package org.cirruslabs.utils.bazel.model.kotlin

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class KotlinTestPackageInfo(
  fullyQualifiedName: String,
  targetPath: String,
  val testNames: List<String>
) : PackageInfo(fullyQualifiedName, targetPath, "tests") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    val dependencyTargets = (directPackageDependencies + fullyQualifiedName)
      .map { packageRegistry.findInfo(it) }
      .flatten()
    val dependencyPaths = dependencyTargets.mapNotNull { pkg ->
      when {
        pkg is KotlinTestPackageInfo && pkg.fullyQualifiedName.contentEquals(fullyQualifiedName) -> null
        pkg is KotlinTestPackageInfo -> "//${pkg.targetPath}:lib"
        else -> pkg.fullTargetLocation
      }
    }
    val deps = dependencyPaths.toSortedSet().joinToString(separator = "\n") { "\"$it\"," }
    val result = StringBuilder()
    result.append("""# GENERATED
load("@io_bazel_rules_kotlin//kotlin:kotlin.bzl", "kt_jvm_library", "kt_jvm_test")

kt_jvm_library(
  name = "lib",
  srcs = glob(["*.kt"]),
  visibility = ["//visibility:public"],
  deps = [
${deps.prependIndent("    ")}
  ],
)
"""
    )

    testNames.forEach { testName ->
      result.append("""
kt_jvm_test(
  name = "$testName",
  srcs = ["$testName.kt"],
  test_class = "$fullyQualifiedName.$testName",
  visibility = ["//visibility:public"],
  deps = [ ":lib" ],
)"""
      )
    }
    return result.toString()
  }
}
