package org.cirruslabs.utils.bazel.fetcher

import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.junit.Test
import kotlin.test.assertEquals


@KtorExperimentalAPI
class MavenInfoFetcherTest {
  private val fetcher = MavenInfoFetcher()

  @Test
  fun testApacheCommons(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("https://maven-central.storage.googleapis.com/repos/central/data/commons-lang/commons-lang/2.6/commons-lang-2.6.jar")
    assertEquals(listOf("org.apache.commons.lang"), fetchedPackages)
  }

  @Test
  fun testKotlinCoroutines(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("https://repo1.maven.org/maven2/org/jetbrains/kotlinx/kotlinx-coroutines-core/1.3.6/kotlinx-coroutines-core-1.3.6.jar")
    assertEquals(listOf("kotlinx.coroutines"), fetchedPackages)
  }

  @Test
  fun testKtor(): Unit = runBlocking {
    val fetchedPackages = fetcher.findPackagesInMavenArtifact("https://repo1.maven.org/maven2/io/ktor/ktor-client-core/1.3.1/ktor-client-core-1.3.1.jar")
    assertEquals(listOf("io.ktor.client", "io.ktor.network.sockets"), fetchedPackages)
  }
}
