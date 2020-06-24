package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.java.JavaPackageInfo
import java.nio.file.Path

class JavaPackageCollector(workspaceRoot: Path) : AbstractJVMPackageCollector<JavaPackageInfo>(workspaceRoot) {
  override val subRoot: String
    get() = "main/java"
  override val jvmFileExtension: String
    get() = "java"

  override fun buildFilePath(packageInfo: JavaPackageInfo): Path =
    workspaceRoot.resolve(packageInfo.targetPath).resolve("BUILD.bazel")

  override fun generateBuildFileContent(registry: PackageRegistry, packageInfo: JavaPackageInfo): String =
    packageInfo.generateBuildFile(registry)

  override fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<AbstractJVMPackageCollector.Companion.JvmFile>): JavaPackageInfo {
    val packageInfo = JavaPackageInfo(fullyQualifiedName = fqn, targetPath = relativePackagePath.toString())
    packageInfo.addDirectPackageDependencies(directPackageDependencies)
    return packageInfo
  }
}
