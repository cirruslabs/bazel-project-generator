package org.cirruslabs.utils.bazel.collector

import org.cirruslabs.utils.bazel.model.base.PackageRegistry
import org.cirruslabs.utils.bazel.model.proto.KotlinProtoInfo
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class ProtoPackageCollector(private val workspaceRoot: Path) {
  companion object {
    data class ProtoFile(
      val path: Path,
      val packageFqName: String
    )
  }

  fun collectPackageInfoInSourceRoot(registry: PackageRegistry, sourceRoots: List<Path>) {
    val allProtoFiles = sourceRoots
      .map { it.resolve("main/proto") }
      .filter { Files.exists(it) }
      .map {
        val allJvmFiles: MutableList<ProtoFile> = mutableListOf()
        Files.walkFileTree(it, object : SimpleFileVisitor<Path>() {
          override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            if (!file.toString().endsWith(".proto")) return FileVisitResult.CONTINUE
            allJvmFiles.add(parseProtoFile(file))
            return FileVisitResult.CONTINUE
          }
        })
        allJvmFiles
      }
      .flatten()
    allProtoFiles
      .groupBy { it.packageFqName }
      .forEach { (packageFqName, files) ->
        val packageFolders = files.map { it.path.parent }.toSet()
        if (packageFolders.size > 1) {
          System.err.println("Package $packageFqName is declared in multiple folders: ${packageFolders}")
        }
        val packagePath = packageFolders.first()
        val relativePackagePath =
          if (packagePath.isAbsolute) workspaceRoot.relativize(packagePath) else packagePath
        val packageInfo = KotlinProtoInfo(packageFqName, relativePackagePath.toString())
        registry.addTarget(packageFqName, packageInfo)
      }
  }

  private fun parseProtoFile(path: Path): ProtoFile {
    val line = Files.readAllLines(path).find { line ->
      line.startsWith("option java_package")
    } ?: ""
    val packageFqName = line.substringAfter('"').substringBefore('"')
    return ProtoFile(path, packageFqName)
  }

  fun generateBuildFiles(registry: PackageRegistry) {
    for (packageInfo in registry.packages) {
      val protoInfo = packageInfo as? KotlinProtoInfo ?: continue
      val buildFilePath = workspaceRoot.resolve(protoInfo.targetPath).resolve("BUILD.bazel")
      if (Files.exists(buildFilePath) && !Files.deleteIfExists(buildFilePath)) {
        System.err.println("Failed to delete $buildFilePath")
      } else {
        println("Generating $buildFilePath")
        Files.writeString(
          buildFilePath,
          protoInfo.generateBuildFile(registry),
          StandardOpenOption.CREATE_NEW
        )
      }
    }
  }
}
