load("@rules_jvm_external//:defs.bzl", "maven_install")

def jvm_dependencies():
  maven_install(
    artifacts = [
      "com.google.code.gson:gson:2.8.6",
      "commons-lang:commons-lang:2.6",
      "junit:junit:4.13",
    ],
    repositories = [
      "https://maven-central.storage.googleapis.com/repos/central/data/",
      "https://repo1.maven.org/maven2",
    ],
  )
