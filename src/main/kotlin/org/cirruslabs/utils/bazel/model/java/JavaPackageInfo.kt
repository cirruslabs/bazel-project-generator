package org.cirruslabs.utils.bazel.model.java

import org.cirruslabs.utils.bazel.model.base.PackageInfo
import org.cirruslabs.utils.bazel.model.base.PackageRegistry

class JavaPackageInfo(
  fullyQualifiedName: String,
  targetPath: String
) : PackageInfo(fullyQualifiedName, targetPath, "java") {
  override fun generateBuildFile(packageRegistry: PackageRegistry): String {
    val deps = directPackageDependencies
      .map { packageRegistry.findInfo(it) }
      .flatten()
      .map { it.fullTargetLocation }
      .toSortedSet().joinToString(separator = "\n") { "\"$it\"," }
    return """# GENERATED
java_library(
  name = "$targetName",
  srcs = glob(["*.java"]),
  visibility = ["//visibility:public"],
  deps = [
${deps.prependIndent("    ")}
  ],
)"""
  }
}
