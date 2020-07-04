package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.BazelTarget
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

abstract class AbstractJVMPackageCollector<T : BazelTarget>(
  protected val workspaceRoot: Path
) {
  companion object {
    data class JvmFile(
      val path: Path,
      val packageFqName: String,
      val importedPackages: Set<String>
    )

    private val defaultKotlinPackageImports = setOf(
      "kotlin",
      "kotlin.annotation",
      "kotlin.collections",
      "kotlin.comparisons",
      "kotlin.io",
      "kotlin.ranges",
      "kotlin.sequences",
      "kotlin.text",
      "kotlin.test"
    )
  }

  abstract val subRoot: String
  abstract val jvmFileExtension: String
  abstract fun buildFilePath(packageInfo: T): Path
  abstract fun generateBuildFileContent(registry: PackageRegistry, packageInfo: T): String
  abstract fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<JvmFile>): T

  fun collectPackageInfoInSourceRoot(registry: PackageRegistry, sourceRoots: List<Path>) {
    val allJvmFiles = sourceRoots
      .map { it.resolve(subRoot) }
      .filter { Files.exists(it) }
      .map {
        val allJvmFiles: MutableList<JvmFile> = mutableListOf()
        Files.walkFileTree(it, object : SimpleFileVisitor<Path>() {
          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (!file.toString().endsWith(".$jvmFileExtension")) return FileVisitResult.CONTINUE
            allJvmFiles.add(parseJvmFile(file))
            return FileVisitResult.CONTINUE
          }
        })
        allJvmFiles
      }
      .flatten()
    allJvmFiles
      .groupBy { it.packageFqName }
      .forEach { (packageFqName, files) ->
        val packageFolders = files.map { it.path.parent }.toSet()
        if (packageFolders.size > 1) {
          System.err.println("Package $packageFqName is declared in multiple folders: ${packageFolders}")
        }
        val packagePath = packageFolders.first()
        val relativePackagePath =
          if (packagePath.isAbsolute) workspaceRoot.relativize(packagePath) else packagePath
        val directPackageDependencies = files.map { it.importedPackages }.flatten().toSortedSet()
        val packageInfo = generateTarget(packageFqName, relativePackagePath, directPackageDependencies, files)
        registry.addTarget(packageFqName, packageInfo)
      }
  }

  private fun parseJvmFile(path: Path): JvmFile {
    var packageFqName = ""
    val importedPackages = mutableSetOf<String>()
    Files.readAllLines(path).forEach { line ->
      if (line.startsWith("package")) {
        packageFqName = line.substringAfter("package")
          .trimStart()
          .substringBefore(' ')
          .removeSuffix(";")
      }
      if (line.startsWith("import")) {
        val importFq = line.substringAfter("import")
          .trimStart()
          .substringBefore(' ')
          .removeSuffix(";")
        importedPackages.add(importFq.substringBeforeLast('.'))
      }
    }
    val importedPackagesFiltered = importedPackages
      .filter { !it.startsWith("java.") }
      .filter { !defaultKotlinPackageImports.contains(it.substringBeforeLast('.')) }
    return JvmFile(path, packageFqName, importedPackagesFiltered.toSortedSet())
  }

  fun generateBuildFiles(registry: PackageRegistry, dryRun: Boolean) {
    for (packageInfo in registry.packages) {
      val buildFilePath = try {
        @Suppress("UNCHECKED_CAST")
        buildFilePath(packageInfo as T)
      } catch (ex: ClassCastException) {
        continue
      }

      println("Generating content of $buildFilePath")
      val buildFileContent = generateBuildFileContent(registry, packageInfo)
      if (dryRun) continue

      if (Files.exists(buildFilePath) && !Files.deleteIfExists(buildFilePath)) {
        System.err.println("Failed to delete $buildFilePath")
      } else {
        Files.writeString(
          buildFilePath,
          buildFileContent,
          StandardOpenOption.CREATE_NEW
        )
      }
    }
  }
}
