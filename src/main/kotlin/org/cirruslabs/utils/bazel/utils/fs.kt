package org.cirruslabs.utils.bazel.utils

import java.nio.file.Files
import java.nio.file.Path

fun folderContainsFiles(file: Path, extension: String): Boolean {
  if (!Files.isDirectory(file)) return false
  return Files.walk(file, 1).anyMatch { path ->
    Files.isRegularFile(path) && path.fileName.toString().endsWith(".$extension")
  }
}
