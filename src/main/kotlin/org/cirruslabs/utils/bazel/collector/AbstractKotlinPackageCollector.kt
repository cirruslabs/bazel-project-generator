package org.cirruslabs.utils.bazel.collector

import kotlinx.coroutines.withContext
import org.cirruslabs.utils.bazel.model.base.BazelTarget
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VfsUtilCore
import org.jetbrains.kotlin.kotlinx.coroutines.Dispatchers
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.lang.ClassCastException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

abstract class AbstractKotlinPackageCollector<T : BazelTarget>(
  protected val workspaceRoot: Path,
  private val environment: KotlinCoreEnvironment
) {
  abstract val subRoot: String
  abstract fun buildFilePath(packageInfo: T): Path
  abstract fun generateBuildFileContent(registry: PackageRegistry, packageInfo: T): String
  abstract fun generateTarget(fqn: String, relativePackagePath: String, directPackageDependencies: Set<String>, files: List<KtFile>): T

  suspend fun collectPackageInfoInSourceRoot(registry: PackageRegistry, sourceRoots: List<Path>) = withContext(Dispatchers.IO) {
    environment.addKotlinSourceRoots(
      sourceRoots
        .map { it.resolve(subRoot) }
        .filter { Files.exists(it) }
        .map { it.toFile() }
    )
    val workspaceRootFile = StandardFileSystems.local().refreshAndFindFileByPath(workspaceRoot.toString())
      ?: throw IllegalStateException("Can't init local FS!")
    for ((fqn, files) in environment.getSourceFiles().groupBy { it.packageFqName.asString() }) {
      // todo: investigate if there is a need to validate if all methods in files are explicitly declaring return types
      val packageFolders = files.map { it.virtualFile.parent }.toSet()
      if (packageFolders.size > 1) {
        System.err.println("Package $fqn is declared in multiple folders: ${packageFolders.map { it.canonicalPath }}")
      }

      val relativePackagePath = VfsUtilCore.getRelativePath(packageFolders.first(), workspaceRootFile, File.separatorChar)
        ?: continue
      val directPackageDependencies = collectDirectPackageDependencies(files)
      val packageInfo = generateTarget(fqn, relativePackagePath, directPackageDependencies, files)
      registry.addTarget(fqn, packageInfo)
    }
  }

  private suspend fun collectDirectPackageDependencies(kotlinFiles: List<KtFile>): Set<String> = withContext(Dispatchers.IO) {
    val result = mutableSetOf<String>()
    for (kotlinFile in kotlinFiles) {
      val importedPackages = kotlinFile.importDirectives.mapNotNull {
        it.importedFqName
      }.map {
        it.parent().asString()
      }.toSet()
      result.addAll(importedPackages)
    }
    result
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
