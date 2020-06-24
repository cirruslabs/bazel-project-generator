package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.kotlin.KotlinPackageInfo
import java.nio.file.Path

class KotlinPackageCollector(workspaceRoot: Path) : AbstractJVMPackageCollector<KotlinPackageInfo>(workspaceRoot) {
  override val subRoot: String
    get() = "main/kotlin"
  override val jvmFileExtension: String
    get() = "kt"

  override fun buildFilePath(packageInfo: KotlinPackageInfo): Path =
    workspaceRoot.resolve(packageInfo.targetPath).resolve("BUILD.bazel")

  override fun generateBuildFileContent(registry: PackageRegistry, packageInfo: KotlinPackageInfo): String =
    packageInfo.generateBuildFile(registry)

  override fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<AbstractJVMPackageCollector.Companion.JvmFile>): KotlinPackageInfo {
    val packageInfo = KotlinPackageInfo(fullyQualifiedName = fqn, targetPath = relativePackagePath.toString())
    packageInfo.addDirectPackageDependencies(directPackageDependencies)
    return packageInfo
  }
}
