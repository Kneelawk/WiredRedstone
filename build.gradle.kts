import com.modrinth.minotaur.dependencies.ModDependency

plugins {
    id("fabric-loom")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    id("io.github.juuxel.loom-quiltflower")
    id("com.modrinth.minotaur")
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

val modVersion: String by project
version = modVersion
val mavenGroup: String by project
group = mavenGroup

loom {
    accessWidenerPath.set(file("src/main/resources/wiredredstone.accesswidener"))
}

repositories {
    maven("https://mod-buildcraft.com/maven") { name = "BuildCraft" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.quiltmc.org/repository/release") { name = "QuiltMC" }
    maven("https://maven.vram.io/") { name = "VRAM" }
    maven("https://maven.shedaniel.me/") { name = "shedaniel" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }
    maven("https://maven.bai.lol") { name = "WTHIT" }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
//    mavenLocal()
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

    // GraphLib dependency
    val graphlibVersion: String by project
    modImplementation("com.kneelawk:graphlib:$graphlibVersion")
    include("com.kneelawk:graphlib:$graphlibVersion")

    // WTHIT API
    val wthitVersion: String by project
    modCompileOnly("mcp.mobius.waila:wthit-api:fabric-$wthitVersion")

    // CC: Restitched
    val ccRestitchedVersion: String by project
    modCompileOnly("maven.modrinth:cc-restitched:$ccRestitchedVersion")

    //
    // Optional Mod Dependencies
    //

    // Mod Menu
    val modMenuVersion: String by project
    modRuntimeOnly("com.terraformersmc:modmenu:$modMenuVersion") {
        exclude("net.fabricmc.fabric-api")
    }

    // WTHIT
    modRuntimeOnly("mcp.mobius.waila:wthit:fabric-$wthitVersion") {
        exclude("net.fabricmc.fabric-api")
    }

    // CC: Restitched
    modRuntimeOnly("maven.modrinth:cc-restitched:$ccRestitchedVersion")
    val clothConfigVersion: String by project
    modRuntimeOnly("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude("net.fabricmc.fabric-api")
    }
    val clothApiVersion: String by project
    modRuntimeOnly("me.shedaniel.cloth.api:cloth-utils-v1:$clothApiVersion") {
        exclude("net.fabricmc.fabric-api")
    }
    val nightConfigVersion: String by project
    runtimeOnly("com.electronwill.night-config:toml:$nightConfigVersion")

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

modrinth {
    val commaRegex = Regex("\\s*,\\s*")
    token.set(System.getenv("MODRINTH_TOKEN"))
    val mrProjectId: String by project
    projectId.set(mrProjectId)
    versionNumber.set(modVersion)
    val mrVersionType: String by project
    versionType.set(mrVersionType)
    val changelogFile = file("changelogs/changelog-v$modVersion.md")
    if (changelogFile.exists()) {
        changelog.set(changelogFile.readText())
    }
    uploadFile.set(tasks.remapJar.get())
    additionalFiles.set(listOf(tasks.getByName("sourcesJar")))
    val mrGameVersions: String by project
    gameVersions.set(mrGameVersions.split(commaRegex))
    val mrLoaders: String by project
    loaders.set(mrLoaders.split(commaRegex))
    val fabricApiMrProjectId: String by project
    val fabricLangKotlinMrProjectId: String by project
    dependencies.set(
        listOf(
            ModDependency(fabricApiMrProjectId, "required"),
            ModDependency(fabricLangKotlinMrProjectId, "required")
        )
    )
    syncBodyFrom.set(rootProject.file("README.md").readText())
}
