package org.cirruslabs.utils.bazel.model

import java.util.concurrent.ConcurrentHashMap

class PackageRegistry {
  private val mapping: MutableMap<String, PackageInfo> = ConcurrentHashMap()

  fun findInfo(fqn: String): PackageInfo? = mapping[fqn]

  fun addPackage(packageInfo: PackageInfo) {
    mapping[packageInfo.fullyQualifiedName] = packageInfo
  }
}
