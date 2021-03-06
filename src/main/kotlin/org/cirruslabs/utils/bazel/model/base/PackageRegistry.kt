package org.cirruslabs.utils.bazel.model.base

import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class PackageRegistry(
  private val mapping: ConcurrentHashMap<String, Queue<BazelTarget>> = ConcurrentHashMap()
) {
  val packages: Set<BazelTarget>
    get() = mapping.values.flatten().toSet()

  fun findInfo(fqn: String): List<BazelTarget> {
    return findInfoImpl(fqn).also {
      if (it.isEmpty()) System.err.println("Didn't find information about $fqn")
    }
  }

  private fun findInfoImpl(fqn: String): List<BazelTarget> {
    if (fqn.isBlank()) return emptyList()
    val parentFQN = fqn.substringBeforeLast('.', missingDelimiterValue = "")
    val packageInfo = mapping[fqn]
    if (packageInfo != null && packageInfo.isNotEmpty()) {
      return packageInfo.toList()
    }
    return findInfoImpl(parentFQN)
  }

  fun addTarget(fqn: String, target: BazelTarget) {
    mapping.getOrPut(fqn) { ConcurrentLinkedQueue<BazelTarget>() }.add(target)
  }

  fun copy(): PackageRegistry {
    val newMapping = ConcurrentHashMap<String, Queue<BazelTarget>>(mapping.size)
    mapping.forEach { (fqn, targets) ->
      newMapping[fqn] = ConcurrentLinkedQueue<BazelTarget>(targets)
    }
    return PackageRegistry(newMapping)
  }
}
