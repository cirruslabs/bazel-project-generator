plugins {
  id("org.jetbrains.kotlin.jvm") version "1.3.72"
  application
}

repositories {
  jcenter()
  mavenCentral()
  maven {
    url = uri("https://maven.springframework.org/release")
  }
}

dependencies {
  implementation("com.google.code.gson:gson:2.8.6")
  implementation("com.github.ajalt:clikt:2.7.1")
  implementation("com.squareup.wire:wire-runtime:3.2.2")
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.7")
  implementation("io.get-coursier:coursier_2.13:2.0.0-RC6-21")

  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
  mainClassName = "org.cirruslabs.utils.bazel.AppKt"
}
