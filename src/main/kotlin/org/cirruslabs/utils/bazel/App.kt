package org.cirruslabs.utils.bazel

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.runBlocking
import org.cirruslabs.utils.bazel.collector.DependenciesCollector
import org.cirruslabs.utils.bazel.collector.KotlinPackageCollector
import org.cirruslabs.utils.bazel.collector.KotlinTestPackageCollector
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

  override fun run() = runBlocking {
    val registry = PackageRegistry()
    val dependenciesCollector = when {
     dependencies != null -> DependenciesCollector.create(dependencies ?: workspaceRoot.resolve("dependencies_jvm.json"))
     Files.exists(workspaceRoot.resolve("dependencies_jvm.json")) -> DependenciesCollector.create(workspaceRoot.resolve("dependencies_jvm.json"))
     else -> null
    }
    dependenciesCollector?.collectPackageInfos(registry)

    val kotlinPackageCollector = KotlinPackageCollector.create(workspaceRoot.toAbsolutePath())
    kotlinPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)

    dependenciesCollector?.generateWorkspaceFile(workspaceRoot)
    kotlinPackageCollector.generateBuildFiles(registry)

    val kotlinTestPackageCollector = KotlinTestPackageCollector.create(workspaceRoot.toAbsolutePath())
    kotlinTestPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)
    kotlinTestPackageCollector.generateBuildFiles(registry)
  }
}

fun main(args: Array<String>) {
  App().main(args)
}
