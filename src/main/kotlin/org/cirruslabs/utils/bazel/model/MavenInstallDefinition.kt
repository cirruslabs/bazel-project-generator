package org.cirruslabs.utils.bazel.model

class MavenInstallDefinition(
  var dependency_tree: MavenDependencyTree
)

data class MavenDependencyTree(
  var dependencies: List<MavenDependency>
)

data class MavenDependency(
  var coord: String,
  var url: String
) {
  val definition: LibraryDefinition
    get() {
      val coordParts = coord.split(':', limit = 3)
      return LibraryDefinition(coordParts[0], coordParts[1], coordParts[2])
    }
}

data class LibraryDefinition(
  val group: String,
  val name: String,
  val version: String
) {
  val mavenCoordinate: String
    get() = "$group:$name:$version"
}
