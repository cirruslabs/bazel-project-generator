package org.cirruslabs.utils.bazel.model

class DependenciesDefinition(
  var repositories: List<String> = emptyList(),
  var libraries: List<LibraryDefinition> = emptyList()
)

data class LibraryDefinition(
  val group: String,
  val name: String,
  val version: String,
  val packagePrefixes: List<String>
) {
  val mavenCoordinate: String
    get() = "$group:$name:$version"
}
