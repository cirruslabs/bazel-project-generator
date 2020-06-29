package org.cirruslabs.utils.bazel

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.path
import org.cirruslabs.utils.bazel.collector.*
import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import java.io.IOException
import java.nio.file.*

class App : CliktCommand() {
  private val workspaceRoot: Path by option(help = "Workspace root")
    .path(canBeFile = false, canBeDir = true)
    .default(Paths.get(""))

  private val dependencies: Path? by option(help = "JSON file with Maven dependencies")
    .path(canBeFile = true, canBeDir = false)

  private val sourceContentRoot: List<Path> by option(help = "Source root to look for targets (can be relative to --workspace-root)")
    .path(canBeFile = false, canBeDir = true)
    .multiple(required = false)

  private val dryRun: Boolean by option(help = "Option to disable files generation")
    .flag(default = false)

  private val protoTargetLanguage: String by option(help = "Either Kotlin or Java to use either grpc-kotlin or grpc-java only")
    .default("Kotlin")

  override fun run() {
    val registry = PackageRegistry()
    val dependenciesCollector = when {
      dependencies != null -> DependenciesCollector(
        dependencies ?: workspaceRoot.resolve("dependencies_jvm.json")
      )
      Files.exists(workspaceRoot.resolve("dependencies_jvm.json")) -> DependenciesCollector(workspaceRoot.resolve("dependencies_jvm.json"))
      else -> null
    }

    val roots =
      if (sourceContentRoot.isEmpty()) {
        findSourceRootsForGradleProject()
      } else {
        sourceContentRoot
      }

    dependenciesCollector?.collectPackageInfos(registry)
    if (!dryRun) {
      dependenciesCollector?.generateWorkspaceFile(workspaceRoot)
    }

    val javaPackageCollector = JavaPackageCollector(workspaceRoot.toAbsolutePath())
    javaPackageCollector.collectPackageInfoInSourceRoot(registry, roots)
    if (!dryRun) {
      javaPackageCollector.generateBuildFiles(registry)
    }

    val registryForJavaTests = registry.copy()
    val javaTestPackageCollector = JavaTestPackageCollector(workspaceRoot.toAbsolutePath())
    javaTestPackageCollector.collectPackageInfoInSourceRoot(registryForJavaTests, roots)
    if (!dryRun) {
      javaTestPackageCollector.generateBuildFiles(registryForJavaTests)
    }

    val protoPackageCollector = ProtoPackageCollector(workspaceRoot.toAbsolutePath(), protoTargetLanguage.toLowerCase())
    protoPackageCollector.collectPackageInfoInSourceRoot(registry, roots)
    if (!dryRun) {
      protoPackageCollector.generateBuildFiles(registry)
    }

    val kotlinPackageCollector = KotlinPackageCollector(workspaceRoot.toAbsolutePath())
    kotlinPackageCollector.collectPackageInfoInSourceRoot(registry, roots)
    if (!dryRun) {
      kotlinPackageCollector.generateBuildFiles(registry)
    }

    val kotlinTestPackageCollector = KotlinTestPackageCollector(workspaceRoot.toAbsolutePath())
    kotlinTestPackageCollector.collectPackageInfoInSourceRoot(registry, roots)
    if (!dryRun) {
      kotlinTestPackageCollector.generateBuildFiles(registry)
    }
  }

  private fun findSourceRootsForGradleProject(): List<Path> {
    val result = mutableListOf<Path>()
    Files.walkFileTree(workspaceRoot, object : SimpleFileVisitor<Path>() {
      override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
        if (dir.endsWith("src")) result.add(dir)
        return FileVisitResult.CONTINUE
      }
    })
    return result
  }
}

fun main(args: Array<String>) {
  App().main(args)
}
