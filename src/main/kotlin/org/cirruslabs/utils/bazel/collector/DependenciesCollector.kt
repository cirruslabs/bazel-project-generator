package org.cirruslabs.utils.bazel.collector

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.cirruslabs.utils.bazel.model.DependenciesDefinition
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.maven.MavenDependencyPackageInfo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

class DependenciesCollector(private val deps: DependenciesDefinition) {
  companion object {
    suspend fun create(dependenciesFile: Path) = withContext(Dispatchers.IO) {
      val def = Gson().fromJson(
        Files.readString(dependenciesFile),
        DependenciesDefinition::class.java
      )
      DependenciesCollector(def)
    }
  }

  fun collectPackageInfos(registry: PackageRegistry) {
    deps.libraries.forEach { library ->
      val mavenPackageInfo = MavenDependencyPackageInfo(library)
      library.packagePrefixes.forEach { prefix ->
        registry.addTarget(prefix, mavenPackageInfo)
      }
    }
  }

  fun generateWorkspaceFile(workspaceRoot: Path) {
    val buildFilePath = workspaceRoot.resolve("3rdparty/jvm").resolve("BUILD")
    if (!Files.exists(buildFilePath)) {
      Files.createDirectories(buildFilePath.parent)
      Files.createFile(buildFilePath)
    }
    val workspaceFilePath = workspaceRoot.resolve("3rdparty/jvm").resolve("workspace.bzl")
    if (Files.exists(workspaceFilePath) && !Files.deleteIfExists(workspaceFilePath)) {
      System.err.println("Failed to delete $workspaceFilePath")
    } else {
      Files.writeString(
        workspaceFilePath,
        workspaceFileContent(),
        StandardOpenOption.CREATE_NEW
      )
    }
  }

  private fun workspaceFileContent(): String {
    val libraries = deps.libraries.map { it.mavenCoordinate }
      .sorted().joinToString(separator = "\n") { "\"$it\"," }
    val repositories = deps.repositories.sorted().joinToString(separator = "\n") { "\"$it\"," }
    return """# GENERATED
load("@rules_jvm_external//:defs.bzl", "maven_install")

def jvm_dependencies():
  maven_install(
    artifacts = [
${libraries.prependIndent("  ").prependIndent("  ").prependIndent("  ")}
    ],
    repositories = [
${repositories.prependIndent("  ").prependIndent("  ").prependIndent("  ")}
    ],
  )"""
  }
}
