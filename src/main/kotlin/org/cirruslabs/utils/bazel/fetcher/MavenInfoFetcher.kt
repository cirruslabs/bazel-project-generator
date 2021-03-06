package org.cirruslabs.utils.bazel.fetcher

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.jar.JarFile

@KtorExperimentalAPI
class MavenInfoFetcher(val caching: Boolean = false) {
  private val httpClient: HttpClient = HttpClient(CIO) {
    followRedirects = true
    expectSuccess = false // to handle 404s
  }

  suspend fun findPackagesInMavenArtifact(jarURL: String): List<String> {
    val jarName = jarURL.substringAfterLast('/')
    val response = httpClient.get<HttpResponse>(jarURL)
    if (!response.status.isSuccess()) return emptyList()
    val file = withContext(Dispatchers.IO) {
      if (caching) {
        val cachedJarPath = Path.of(".cache", "maven", jarName)
        val cachedFile = File(cachedJarPath.toUri())
        if (!cachedFile.exists()) {
          Files.createDirectories(cachedJarPath.parent)
          response.content.copyAndClose(cachedFile.writeChannel())
          println("Cached $jarName in ${cachedJarPath.parent.toAbsolutePath()}!")
        } else {
          println("Found $jarName in caches!")
        }
        cachedFile
      } else {
        File.createTempFile("ktor", "http-client").also {
          it.deleteOnExit()
          response.content.copyAndClose(it.writeChannel())
        }
      }
    }
    return extractTopLevelPackages(file)
  }

  private suspend fun extractTopLevelPackages(file: File): List<String> = withContext(Dispatchers.IO) {
    val jarFile = JarFile(file)
    val allPackages = sortedSetOf<String>()
    jarFile.entries().asIterator().forEach { entry ->
      val name = entry.name
      if (name.endsWith(".class") || name.endsWith(".kotlin_metadata")) {
        allPackages.add(name.substringBeforeLast("/").replace('/', '.'))
      }
    }
    val topLevelPackages = mutableSetOf<String>()
    fun containsAnyParent(packageCandidate: String): Boolean {
      if (packageCandidate.isBlank()) return false
      if (topLevelPackages.contains(packageCandidate)) return true
      return containsAnyParent(packageCandidate.substringBeforeLast('.', missingDelimiterValue = ""))
    }
    allPackages.forEach { packageCandidate ->
      if (!containsAnyParent(packageCandidate)) {
        topLevelPackages.add(packageCandidate)
      }
    }
    topLevelPackages.toList().sorted()
  }
}
