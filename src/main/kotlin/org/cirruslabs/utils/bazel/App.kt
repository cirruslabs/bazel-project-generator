package org.cirruslabs.utils.bazel

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.cirruslabs.utils.bazel.collector.KotlinPackageCollector
import org.cirruslabs.utils.bazel.model.PackageRegistry
import java.nio.file.Path
import java.nio.file.Paths

class App : CliktCommand() {
  private val workspaceRoot: Path by option(help = "Workspace root")
    .path(canBeFile = false, canBeDir = true)
    .default(Paths.get(""))

  private val sourceContentRoot: List<Path> by option(help = "Source root to look for targets")
    .path(canBeFile = false, canBeDir = true)
    .multiple(required = false)

  override fun run() = runBlocking {
    val registry = PackageRegistry()
    val kotlinPackageCollector = KotlinPackageCollector.create(workspaceRoot.toAbsolutePath())
    kotlinPackageCollector.collectPackageInfoInSourceRoot(registry, sourceContentRoot)
    kotlinPackageCollector.generateBuildFiles(registry)
  }
}

fun main(args: Array<String>) {
  App().main(args)
}
