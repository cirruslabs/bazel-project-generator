package org.cirruslabs.utils.bazel

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import org.cirruslabs.utils.bazel.collector.*
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class App : CliktCommand() {
  private val workspaceRoot: Path by option(help = "Workspace root")
    .path(canBeFile = false, canBeDir = true)
    .default(Paths.get(""))

  private val dependencies: Path? by option(help = "JSON file with Maven dependencies")
    .path(canBeFile = true, canBeDir = false)

  private val sourceContentRoot: List<Path> by option(help = "Source root to look for targets")
    .path(canBeFile = false, canBeDir = true)
    .multiple(required = false)

  override fun run() {
    val registry = PackageRegistry()
    val dependenciesCollector = when {
      dependencies != null -> DependenciesCollector(
        dependencies ?: workspaceRoot.resolve("dependencies_jvm.json")
      )
      Files.exists(workspaceRoot.resolve("dependencies_jvm.json")) -> DependenciesCollector(workspaceRoot.resolve("dependencies_jvm.json"))
      else -> null
    }
    dependenciesCollector?.collectPackageInfos(registry)
    dependenciesCollector?.generateWorkspaceFile(workspaceRoot)

    val javaPackageCollector = JavaPackageCollector(workspaceRoot.toAbsolutePath())
    javaPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)
    javaPackageCollector.generateBuildFiles(registry)

    val registryForJavaTests = registry.copy()
    val javaTestPackageCollector = JavaTestPackageCollector(workspaceRoot.toAbsolutePath())
    javaTestPackageCollector.collectPackageInfoInSourceRoot(registryForJavaTests, sourceContentRoot)
    javaTestPackageCollector.generateBuildFiles(registryForJavaTests)

    val kotlinPackageCollector = KotlinPackageCollector(workspaceRoot.toAbsolutePath())
    kotlinPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)
    kotlinPackageCollector.generateBuildFiles(registry)

    val kotlinTestPackageCollector = KotlinTestPackageCollector(workspaceRoot.toAbsolutePath())
    kotlinTestPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)
    kotlinTestPackageCollector.generateBuildFiles(registry)
  }
}

fun main(args: Array<String>) {
  App().main(args)
}
