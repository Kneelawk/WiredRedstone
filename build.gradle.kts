plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    id("io.github.juuxel.loom-quiltflower")
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup

repositories {
    maven("https://mod-buildcraft.com/maven") { name = "BuildCraft" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.quiltmc.org/repository/release") { name = "QuiltMC" }
    maven("https://maven.vram.io/") { name = "VRAM" }
    maven("https://maven.shedaniel.me/") { name = "shedaniel" }
}

dependencies {
    val minecraftVersion: String by project
    minecraft("com.mojang:minecraft:$minecraftVersion")
    val yarnMappings: String by project
    mappings("net.fabricmc:yarn:$yarnMappings:v2")
    val loaderVersion: String by project
    modImplementation("net.fabricmc:fabric-loader:$loaderVersion")
    val fabricVersion: String by project
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricVersion")
    val fabricKotlinVersion: String by project
    modImplementation("net.fabricmc:fabric-language-kotlin:$fabricKotlinVersion")

    // LibMultiPart dependency
    val lmpVersion: String by project
    modImplementation("alexiil.mc.lib:libmultipart-all:$lmpVersion") {
        exclude("net.fabricmc.fabric-api")
    }
    // JIJs LMP, LNS, & LBA Core
    include("alexiil.mc.lib:libmultipart-all:$lmpVersion")

    //
    // Optional Mod Dependencies
    //

    // Mod Menu
    val modMenuVersion: String by project
    modRuntimeOnly("com.terraformersmc:modmenu:$modMenuVersion") {
        exclude("net.fabricmc.fabric-api")
    }

    // Select a renderer
    val renderer = System.getProperty("com.kneelawk.wiredredstone.renderer", "indigo").toLowerCase()

    if (renderer == "canvas") {
        println("Using 'Canvas' renderer.")

        val vramExtension: String by project
        val canvasVersion: String by project
        modRuntimeOnly("io.vram:canvas-fabric-$vramExtension:$canvasVersion") {
            exclude("net.fabricmc.fabric-api")
        }
    }

    if (renderer == "indigo") {
        println("Using 'Indigo' renderer.")
    }

    // Quiltflower
    // Probably best to just use the IDEA plugin instead
//    val quiltflowerVersion: String by project
//    runtimeOnly("org.quiltmc:quiltflower:$quiltflowerVersion")
}

tasks {
    val javaVersion = JavaVersion.VERSION_17

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
        options.release.set(javaVersion.toString().toInt())
    }

    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions { jvmTarget = javaVersion.toString() }
        sourceCompatibility = javaVersion.toString()
        targetCompatibility = javaVersion.toString()
    }

    jar { from("LICENSE") { rename { "${it}_${base.archivesName}" } } }

    processResources {
        inputs.property("version", project.version)

        exclude("**/*.xcf")

        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }

    java {
        toolchain { languageVersion.set(JavaLanguageVersion.of(javaVersion.toString())) }
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }
}
