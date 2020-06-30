# GENERATED
load("@rules_jvm_external//:defs.bzl", "maven_install")

def jvm_dependencies():
  maven_install(
    artifacts = [
      "com.github.ajalt:clikt:2.7.1",
      "com.google.code.gson:gson:2.8.6",
      "io.ktor:ktor-client-cio:1.3.1",
      "io.ktor:ktor-client-core:1.3.1",
      "io.ktor:ktor-http:1.3.1",
      "io.ktor:ktor-io:1.3.1",
      "io.ktor:ktor-utils:1.3.1",
      "junit:junit:4.13",
      "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.6",
    ],
    repositories = [
      "https://maven-central.storage.googleapis.com/repos/central/data/",
      "https://mvnrepository.com/artifact/",
      "https://repo1.maven.org/maven2",
    ],
  )