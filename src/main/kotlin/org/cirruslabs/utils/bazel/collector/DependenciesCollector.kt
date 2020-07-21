package org.cirruslabs.utils.bazel.collector

import com.google.gson.Gson
import io.ktor.util.KtorExperimentalAPI
import org.cirruslabs.utils.bazel.fetcher.MavenInfoFetcher
import org.cirruslabs.utils.bazel.model.MavenInstallDefinition
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.maven.MavenDependencyPackageInfo
import java.nio.file.Files
import java.nio.file.Path

@KtorExperimentalAPI
class DependenciesCollector(private val deps: MavenInstallDefinition, caching: Boolean = false) {
  constructor(dependenciesFile: Path, caching: Boolean) : this(
    Gson().fromJson(
      Files.readString(dependenciesFile),
      MavenInstallDefinition::class.java
    ),
    caching = caching
  )

  private val infoFetcher = MavenInfoFetcher(caching)

  suspend fun collectPackageInfos(registry: PackageRegistry) {
    deps.dependency_tree.dependencies.forEach { mavenDependency ->
      val mavenPackageInfo = MavenDependencyPackageInfo(mavenDependency.definition)
      val libraryPackages = infoFetcher.findPackagesInMavenArtifact(mavenDependency.url)
      if (libraryPackages.isEmpty()) {
        System.err.println("Can't find packages for ${mavenDependency.coord} in defined Maven repositories!")
      } else {
        println("Found the following packages for ${mavenDependency.coord}: $libraryPackages")
      }
      libraryPackages.forEach { prefix ->
        registry.addTarget(prefix, mavenPackageInfo)
      }
    }
  }
}
