package org.cirruslabs.utils.bazel.model.base

abstract class PackageInfo(
  val fullyQualifiedName: String,
  val targetPath: String,
  val targetName: String
) : BazelTarget {
  protected val directPackageDependencies: MutableSet<String> = sortedSetOf()

  override val fullTargetLocation: String
    get() = "//$targetPath:$targetName"

  fun addDirectPackageDependencies(importedPackages: Set<String>) {
    directPackageDependencies.addAll(importedPackages)
  }

  abstract fun generateBuildFile(packageRegistry: PackageRegistry): String
}
