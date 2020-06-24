package org.cirruslabs.utils.bazel.collector

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cirruslabs.utils.bazel.model.base.BazelTarget
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*

abstract class AbstractJVMPackageCollector<T : BazelTarget>(
  protected val workspaceRoot: Path
) {
  companion object {
    data class JvmFile(
      val path: Path,
      val packageFqName: String,
      val importedPackages: Set<String>
    )
  }

  abstract val subRoot: String
  abstract val jvmFileExtension: String
  abstract fun buildFilePath(packageInfo: T): Path
  abstract fun generateBuildFileContent(registry: PackageRegistry, packageInfo: T): String
  abstract fun generateTarget(fqn: String, relativePackagePath: Path, directPackageDependencies: Set<String>, files: List<JvmFile>): T

  suspend fun collectPackageInfoInSourceRoot(registry: PackageRegistry, sourceRoots: List<Path>) = withContext(Dispatchers.IO) {
    val allJvmFiles = sourceRoots
      .map { it.resolve(subRoot) }
      .filter { Files.exists(it) }
      .map {
        val allJvmFiles: MutableList<JvmFile> = LinkedList()
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
        val relativePackagePath = workspaceRoot.relativize(packageFolders.first())
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
      }
      if (line.startsWith("import")) {
        val importFq = line.substringAfter("import")
          .trimStart()
          .substringBefore(' ')
        importedPackages.add(importFq.substringBeforeLast('.'))
      }
    }
    return JvmFile(path, packageFqName, importedPackages)
  }

  suspend fun generateBuildFiles(registry: PackageRegistry) = withContext(Dispatchers.IO) {
    for (packageInfo in registry.packages) {
      val buildFilePath = try {
        @Suppress("UNCHECKED_CAST")
        buildFilePath(packageInfo as T)
      } catch (ex: ClassCastException) {
        continue
      }
      if (Files.exists(buildFilePath) && !Files.deleteIfExists(buildFilePath)) {
        System.err.println("Failed to delete $buildFilePath")
      } else {
        Files.writeString(
          buildFilePath,
          generateBuildFileContent(registry, packageInfo),
          StandardOpenOption.CREATE_NEW
        )
      }
    }
  }
}
