pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net") { name = "Fabric" }
        mavenCentral()
        gradlePluginPortal()
        maven("https://server.bbkr.space/artifactory/libs-release/") { name = "Cotton" }
    }
    plugins {
        val loomVersion: String by settings
        id("fabric-loom") version loomVersion
        val kotlinVersion: String by System.getProperties()
        kotlin("jvm") version kotlinVersion
        val loomQuiltflowerVersion: String by settings
        id("io.github.juuxel.loom-quiltflower") version loomQuiltflowerVersion
        val minotaurVersion: String by settings
        id("com.modrinth.minotaur") version minotaurVersion
        val curseGradleVersion: String by settings
        id("com.matthewprenger.cursegradle") version curseGradleVersion
    }
}

rootProject.name = "wired-redstone"
