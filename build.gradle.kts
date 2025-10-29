import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val pluginGroup: String by project
val pluginVersion: String by project
val javaVersion: String by project
val kotlinVersion: String by project
val intellijVersion: String by project
val intellijType: String by project

plugins {
    kotlin("jvm") version "2.2.0"
    id("org.jetbrains.intellij") version "1.17.3"
}

group = pluginGroup
version = pluginVersion

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(javaVersion.toInt())
}

intellij {
    version.set(intellijVersion)
    type.set(intellijType)
    downloadSources.set(false)
    plugins.set(listOf("java"))
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion))
}

tasks {
    patchPluginXml {
        sinceBuild.set("252.27397")
        untilBuild.set("252.*")
    }
}
