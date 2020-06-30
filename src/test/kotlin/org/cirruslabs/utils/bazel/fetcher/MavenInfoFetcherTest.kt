package org.cirruslabs.utils.bazel.fetcher

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals


@KtorExperimentalAPI
class MavenInfoFetcherTest {
  private val fetcher = MavenInfoFetcher(listOf("https://repo1.maven.org/maven2"))

  @Test
  fun testApacheCommons(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("commons-codec", "commons-codec", "1.14")
    assertEquals(listOf("org.apache.commons.codec"), fetchedPackages)
  }

  @Test
  fun testKotlinCoroutines(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "1.3.6")
    assertEquals(listOf("kotlinx.coroutines"), fetchedPackages)
  }

  @Test
  fun testKtor(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("io.ktor", "ktor-client-core", "1.3.1")
    assertEquals(listOf("io.ktor.client", "io.ktor.network.sockets"), fetchedPackages)
  }
}
