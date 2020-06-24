package org.cirruslabs.utils.bazel.model.java

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class JavaTestPackageInfo(
  fullyQualifiedName: String,
  targetPath: String,
  val testNames: List<String>
) : PackageInfo(fullyQualifiedName, targetPath, "tests") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    val deps = (directPackageDependencies + fullyQualifiedName)
      .map { packageRegistry.findInfo(it) }
      .flatten().mapNotNull { pkg ->
        when {
          pkg is JavaTestPackageInfo && pkg.fullyQualifiedName == this.fullyQualifiedName -> null
          pkg is JavaTestPackageInfo -> "//$targetPath:lib"
          else -> pkg.fullTargetLocation
        }
      }.toSortedSet().joinToString(separator = "\n") { "\"$it\"," }
    val result = StringBuilder()
    result.append("""# GENERATED
java_library(
  name = "lib",
  srcs = glob(["*.java"]),
  visibility = ["//visibility:public"],
  deps = [
${deps.prependIndent("    ")}
  ],
)
"""
    )

    testNames.forEach { testName ->
      result.append("""
java_test(
  name = "$testName",
  srcs = ["$testName.java"],
  test_class = "$fullyQualifiedName.$testName",
  visibility = ["//visibility:public"],
  deps = [ 
    ":lib",
${deps.prependIndent("    ")}
],
)"""
      )
    }
    return result.toString()
  }
}
