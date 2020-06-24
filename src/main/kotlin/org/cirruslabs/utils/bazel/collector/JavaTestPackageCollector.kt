package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.java.JavaTestPackageInfo
import java.nio.file.Path

class JavaTestPackageCollector(
  workspaceRoot: Path
) : AbstractJVMPackageCollector<JavaTestPackageInfo>(workspaceRoot) {

  override val subRoot: String
    get() = "test/java"
  override val jvmFileExtension: String
    get() = "java"

  override fun buildFilePath(packageInfo: JavaTestPackageInfo): Path =
    workspaceRoot.resolve(packageInfo.targetPath).resolve("BUILD.bazel")

  override fun generateBuildFileContent(registry: PackageRegistry, packageInfo: JavaTestPackageInfo): String =
    packageInfo.generateBuildFile(registry)

  override fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<Companion.JvmFile>): JavaTestPackageInfo {
    val packageInfo = JavaTestPackageInfo(
      fullyQualifiedName = fqn,
      targetPath = relativePackagePath.toString(),
      testNames = files.filter { it.path.toString().endsWith("Test.$jvmFileExtension") }.map { it.path.fileName.toString().substringBeforeLast('.') }
    )
    packageInfo.addDirectPackageDependencies(directPackageDependencies)
    return packageInfo
  }
}
