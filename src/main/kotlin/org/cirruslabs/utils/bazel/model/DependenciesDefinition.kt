package org.cirruslabs.utils.bazel.model

class DependenciesDefinition(
  var repositories: List<String> = listOf(
    "https://mvnrepository.com/artifact/",
    "https://maven-central.storage.googleapis.com/repos/central/data/"
  ),
  var libraries: List<LibraryDefinition> = emptyList()
)

data class LibraryDefinition(
  val group: String,
  val name: String,
  val version: String
) {
  val mavenCoordinate: String
    get() = "$group:$name:$version"
}
