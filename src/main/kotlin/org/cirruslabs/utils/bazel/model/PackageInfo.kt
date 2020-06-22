package org.cirruslabs.utils.bazel.model

import java.util.*

abstract class PackageInfo(val fullyQualifiedName: String, val targetPath: String, val targetName: String) {
  protected val directPackageDependencies: MutableSet<String> = TreeSet<String>()

  val fullTargetLocation
    get() = "//$targetPath:$targetName"

  fun addDirectPackageDependencies(importedPackages: Set<String>) {
    directPackageDependencies.addAll(importedPackages)
  }

  abstract fun generateBuildFile(packageRegistry: PackageRegistry): String
}
