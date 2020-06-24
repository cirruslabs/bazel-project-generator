package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.kotlin.KotlinTestPackageInfo
import java.nio.file.Path

class KotlinTestPackageCollector(
  workspaceRoot: Path
) : AbstractJVMPackageCollector<KotlinTestPackageInfo>(workspaceRoot) {

  override val subRoot: String
    get() = "test/kotlin"
  override val jvmFileExtension: String
    get() = "kt"

  override fun buildFilePath(packageInfo: KotlinTestPackageInfo): Path =
    workspaceRoot.resolve(packageInfo.targetPath).resolve("BUILD.bazel")

  override fun generateBuildFileContent(registry: PackageRegistry, packageInfo: KotlinTestPackageInfo): String =
    packageInfo.generateBuildFile(registry)

  override fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<AbstractJVMPackageCollector.Companion.JvmFile>): KotlinTestPackageInfo {
    val packageInfo = KotlinTestPackageInfo(
      fullyQualifiedName = fqn,
      targetPath = relativePackagePath.toString(),
      testNames = files.filter { it.path.toString().endsWith("Test.$jvmFileExtension") }.map { it.path.fileName.toString().substringBeforeLast('.') }
    )
    packageInfo.addDirectPackageDependencies(directPackageDependencies)
    return packageInfo
  }
}
