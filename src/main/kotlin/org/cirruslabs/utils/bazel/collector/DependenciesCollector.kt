package org.cirruslabs.utils.bazel.collector

import com.google.gson.Gson
import io.ktor.util.KtorExperimentalAPI
import org.cirruslabs.utils.bazel.fetcher.MavenInfoFetcher
import org.cirruslabs.utils.bazel.model.DependenciesDefinition
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.maven.MavenDependencyPackageInfo
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

@KtorExperimentalAPI
class DependenciesCollector(private val deps: DependenciesDefinition, caching: Boolean = false) {
  constructor(dependenciesFile: Path, caching: Boolean) : this(
    Gson().fromJson(
      Files.readString(dependenciesFile),
      DependenciesDefinition::class.java
    ),
    caching = caching
  )

  private val infoFetcher = MavenInfoFetcher(deps.repositories, caching)

  suspend fun collectPackageInfos(registry: PackageRegistry) {
    deps.libraries.forEach { library ->
      val mavenPackageInfo = MavenDependencyPackageInfo(library)
      val libraryPackages = infoFetcher.findPackagesInMavenArtifact(library.group, library.name, library.version)
      if (libraryPackages.isEmpty()) {
        System.err.println("Can't find packages for $library in defined Maven repositories!")
      } else {
        println("Found the following packages for $library: $libraryPackages")
      }
      libraryPackages.forEach { prefix ->
        registry.addTarget(prefix, mavenPackageInfo)
      }
    }
  }

  fun generateWorkspaceFiles(workspaceRoot: Path, dryRun: Boolean) {
    val workspaceFileContent = workspaceFileContent()
    if (dryRun) return

    val buildFilePath = workspaceRoot.resolve("3rdparty/jvm").resolve("BUILD.bazel")
    if (!Files.exists(buildFilePath)) {
      Files.createDirectories(buildFilePath.parent)
      Files.createFile(buildFilePath)
    }
    val workspaceFilePath = workspaceRoot.resolve("3rdparty/jvm").resolve("workspace.bzl")
    if (Files.exists(workspaceFilePath) && !Files.deleteIfExists(workspaceFilePath)) {
      System.err.println("Failed to delete $workspaceFilePath")
    } else {
      println("Generating $workspaceFilePath")
      Files.writeString(
        workspaceFilePath,
        workspaceFileContent,
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
