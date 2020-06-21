package org.cirruslabs.utils.bazel.collector

import kotlinx.coroutines.withContext
import org.cirruslabs.utils.bazel.model.PackageInfo
import org.cirruslabs.utils.bazel.model.PackageRegistry
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VfsUtilCore
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.kotlinx.coroutines.Dispatchers
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class KotlinPackageCollector(
  private val workspaceRoot: Path,
  private val environment: KotlinCoreEnvironment
) {
  companion object {
    suspend fun create(workspaceRoot: Path) = withContext(Dispatchers.IO) {
      val environment = KotlinCoreEnvironment.createForProduction(
        Disposer.newDisposable(),
        CompilerConfiguration().apply {
          val messageCollector = PrintingMessageCollector(System.err, MessageRenderer.PLAIN_RELATIVE_PATHS, true)
          put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        },
        EnvironmentConfigFiles.JVM_CONFIG_FILES
      )
      KotlinPackageCollector(workspaceRoot, environment)
    }
  }


  suspend fun collectPackageInfoInSourceRoot(registry: PackageRegistry, sourceRoots: List<Path>) = withContext(Dispatchers.IO) {
    environment.addKotlinSourceRoots(
      sourceRoots
        .map { it.resolve("main/kotlin") }
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
      val packageInfo = PackageInfo(
        fullyQualifiedName = fqn,
        targetPath = relativePackagePath,
        targetName = "generated"
      )
      packageInfo.addDirectPackageDependencies(collectDirectPackageDependencies(files))
      registry.addPackage(packageInfo)
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

  fun generateBuildFiles(registry: PackageRegistry) {
    TODO("Not yet implemented")
  }
}
