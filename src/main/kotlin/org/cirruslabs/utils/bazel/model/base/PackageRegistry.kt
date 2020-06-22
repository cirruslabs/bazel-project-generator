package org.cirruslabs.utils.bazel.model.base

import java.util.concurrent.ConcurrentHashMap

class PackageRegistry {
  private val mapping: MutableMap<String, BazelTarget> = ConcurrentHashMap()

  val packages: Set<BazelTarget>
    get() = mapping.values.toSet()

  fun findInfo(fqn: String): BazelTarget? {
    if (fqn.isBlank()) return null
    val packageInfo = mapping[fqn]
    if (packageInfo != null) {
      return packageInfo
    }
    val parentFQN = fqn.substringBeforeLast('.', missingDelimiterValue = "")
    return findInfo(parentFQN)
  }

  fun addTarget(fql:String, target: BazelTarget) {
    mapping[fql] = target
  }
}
