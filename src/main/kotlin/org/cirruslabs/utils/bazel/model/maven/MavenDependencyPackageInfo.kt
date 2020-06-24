package org.cirruslabs.utils.bazel.model.maven

import org.cirruslabs.utils.bazel.model.LibraryDefinition
import org.cirruslabs.utils.bazel.model.base.BazelTarget

class MavenDependencyPackageInfo(
  private val library: LibraryDefinition
) : BazelTarget {
  override val fullTargetLocation: String
    get() = "@maven//:${library.group}_${library.name}"
      .replace('.', '_')
      .replace('-', '_')
}
