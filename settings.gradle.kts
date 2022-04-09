pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
        maven("https://server.bbkr.space/artifactory/libs-release/") { name = "Cotton" }
    }
    plugins {
        val loomVersion: String by settings
        id("fabric-loom").version(loomVersion)
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm").version(kotlinVersion)
        val loomQuiltflowerVersion: String by settings
        id("io.github.juuxel.loom-quiltflower").version(loomQuiltflowerVersion)
    }
}

rootProject.name = "WiredRedstone"
