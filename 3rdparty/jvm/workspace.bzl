# GENERATED
load("@rules_jvm_external//:defs.bzl", "maven_install")

def jvm_dependencies():
  maven_install(
    artifacts = [
      "com.github.ajalt:clikt:2.7.1",
      "com.google.code.gson:gson:2.8.6",
    ],
    repositories = [
      "https://maven-central.storage.googleapis.com/repos/central/data/",
      "https://repo1.maven.org/maven2",
    ],
  )
