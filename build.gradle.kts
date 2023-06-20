import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation
import com.matthewprenger.cursegradle.CurseUploadTask
import com.matthewprenger.cursegradle.Options
import com.modrinth.minotaur.dependencies.ModDependency

plugins {
    id("fabric-loom")
    id("com.github.johnrengelman.shadow")
    val kotlinVersion: String by System.getProperties()
    kotlin("jvm").version(kotlinVersion)
    `maven-publish`
    id("io.github.juuxel.loom-quiltflower")
    id("com.modrinth.minotaur")
    id("com.matthewprenger.cursegradle")
}

base {
    val archivesBaseName: String by project
    archivesName.set(archivesBaseName)
}

// System to get the release version if this project is being built as part of a release
val modVersion: String = if (System.getenv("RELEASE_TAG") != null) {
    val releaseTag = System.getenv("RELEASE_TAG")
    val modVersion = releaseTag.substring(1)
    println("Detected Release Version: $modVersion")
    modVersion
} else {
    val modVersion: String by project
    println("Detected Local Version: $modVersion")
    modVersion
}
version = modVersion
val mavenGroup: String by project
group = mavenGroup

loom {
    accessWidenerPath.set(file("src/main/resources/wiredredstone.accesswidener"))

//    enableTransitiveAccessWideners.set(false)
}

repositories {
    maven("https://mod-buildcraft.com/maven") { name = "BuildCraft" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.quiltmc.org/repository/release") { name = "QuiltMC" }
    maven("https://maven.vram.io/") { name = "VRAM" }
    maven("https://maven.shedaniel.me/") { name = "shedaniel" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }
    maven("https://maven.bai.lol") { name = "WTHIT" }
    maven("https://squiddev.cc/maven") { name = "SquidDev" }
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content {
            includeGroup("maven.modrinth")
        }
    }
    maven("https://jitpack.io") {
        name = "JitPack"
        content {
            includeGroup("com.github.LlamaLad7")
            includeGroup("com.github.llamalad7.mixinextras")

            // Create stuff
            includeGroup("com.github.AlphaMode")
            includeGroup("com.github.Chocohead")
            includeGroup("com.github.Draylar.omega-config")
        }
    }

    // Create Stuff
    maven("https://mvn.devos.one/snapshots/") { name = "Create" }
    maven("https://ladysnake.jfrog.io/artifactory/mods") { name = "Ladysnake" }
    maven("https://maven.tterrag.com/") { name = "Flywheel" }
    maven("https://maven.cafeteria.dev/releases/") { name = "Cafeteria" } // for Fake Player API
    maven("https://maven.jamieswhiteshirt.com/libs-release") // for Reach Entity Attributes
    maven("https://cursemaven.com") { // for Forge Config API Port
        name = "Cursemaven"
        content {
            includeGroup("curse.maven")
        }
    }

    mavenLocal()
}

val shadowInclude = configurations.create("shadowInclude")

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

    // Mixin Extras
    val mixinExtrasVersion: String by project
    implementation("com.github.llamalad7.mixinextras:mixinextras-fabric:$mixinExtrasVersion")
    annotationProcessor("com.github.llamalad7.mixinextras:mixinextras-fabric:$mixinExtrasVersion")
    include("com.github.llamalad7.mixinextras:mixinextras-fabric:$mixinExtrasVersion")

    // LibMultiPart dependency
    val lmpVersion: String by project
    modImplementation("alexiil.mc.lib:libmultipart-all:$lmpVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }
    // JIJs LMP, LNS, & LBA Core
    include("alexiil.mc.lib:libmultipart-all:$lmpVersion")

    // GraphLib dependency
    val graphlibVersion: String by project
    modImplementation("com.kneelawk:graphlib:$graphlibVersion")
    include("com.kneelawk:graphlib:$graphlibVersion")

    // LMP Compat
    val lmpCompatVersion: String by project
    modImplementation("com.kneelawk:lmp-compat:$lmpCompatVersion")
    include("com.kneelawk:lmp-compat:$lmpCompatVersion")

    // TechReborn Lightweight Energy API
    val energyVersion: String by project
    modApi("teamreborn:energy:$energyVersion") {
        exclude("net.fabricmc.fabric-api")
        exclude("net.fabricmc")
    }
    include("teamreborn:energy:$energyVersion")

    // Quilt-Config
    val quiltConfigVersion: String by project
    implementation("org.quiltmc:quilt-config:$quiltConfigVersion")
    shadowInclude("org.quiltmc:quilt-config:$quiltConfigVersion")
    val quiltJson5Version: String by project
    implementation("org.quiltmc:quilt-json5:$quiltJson5Version")
    shadowInclude("org.quiltmc:quilt-json5:$quiltJson5Version")

    // WTHIT API
    val wthitVersion: String by project
    modCompileOnly("mcp.mobius.waila:wthit-api:fabric-$wthitVersion")

    // CC: Restitched
    val ccRestitchedVersion: String by project
    modCompileOnly("maven.modrinth:cc-restitched:$ccRestitchedVersion")

    // REI
    val reiVersion: String by project
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
    modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")

    // EMI
    val emiVersion: String by project
    modCompileOnly("dev.emi:emi:$emiVersion") {
        isTransitive = false
    }

    // Create
    val createVersion: String by project
    val createMinecraftVersion: String by project
    modCompileOnly("com.simibubi.create:create-fabric-$createMinecraftVersion:$createVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }

    //
    // Optional Mod Dependencies
    //

    // Mod Menu
    val modMenuVersion: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$modMenuVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }

    // WTHIT
    modLocalRuntime("mcp.mobius.waila:wthit:fabric-$wthitVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }

    // CC: Restitched
    modLocalRuntime("maven.modrinth:cc-restitched:$ccRestitchedVersion")
    val clothConfigVersion: String by project
    modLocalRuntime("me.shedaniel.cloth:cloth-config-fabric:$clothConfigVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }
    val clothApiVersion: String by project
    modLocalRuntime("me.shedaniel.cloth.api:cloth-utils-v1:$clothApiVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }
    val cobaltVersion: String by project
    modLocalRuntime("org.squiddev:Cobalt:$cobaltVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
    }
    val nettyVersion: String by project
    runtimeOnly("io.netty:netty-codec-http:$nettyVersion")
    val nightConfigVersion: String by project
    runtimeOnly("com.electronwill.night-config:toml:$nightConfigVersion")

    // REI
//    modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion") {
//        exclude("net.fabricmc.fabric-api")
//    }

    // EMI
    modLocalRuntime("dev.emi:emi:$emiVersion") {
        isTransitive = false
    }

    // Create
    modLocalRuntime("com.simibubi.create:create-fabric-$createMinecraftVersion:$createVersion") {
        exclude("net.fabricmc")
        exclude("net.fabricmc.fabric-api")
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
    }

    jar { from("LICENSE") { rename { "${it}_${base.archivesName.get()}" } } }

    processResources {
        inputs.property("version", project.version)

        exclude("**/*.xcf")

        filesMatching("fabric.mod.json") { expand(mutableMapOf("version" to project.version)) }
    }

    java {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
        withSourcesJar()
    }

    shadowJar {
        configurations = listOf(shadowInclude)
        archiveClassifier.set("shadow-dev")
        dependencies {
            exclude(dependency("org.jetbrains:annotations"))
        }
        relocate("org.quiltmc.config", "com.kneelawk.wiredredstone.shadowlibs.org.quiltmc.config")
        relocate("org.quiltmc.json5", "com.kneelawk.wiredredstone.shadowlibs.org.quiltmc.json5")
        destinationDirectory.set(File(project.buildDir, "devlibs"))
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile)
    }

    withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    withType<CurseUploadTask>().configureEach {
        doLast {
            file("curse-file-id.txt").printWriter().use { file ->
                file.print(mainArtifact.fileID)
            }
        }
    }
}

val commaRegex = Regex("\\s*,\\s*")

modrinth {
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

System.getenv("CURSE_API_KEY")?.let { curseApiKey ->
    curseforge {
        apiKey = curseApiKey
        project(closureOf<CurseProject> {
            id = "639871"
            changelogType = "markdown"
            changelog = project.file("changelogs/changelog-v${modVersion}.md")
            val cfReleaseType: String by project
            releaseType = cfReleaseType
            val cfMinecraftVersions: String by project
            cfMinecraftVersions.split(commaRegex).forEach {
                addGameVersion(it)
            }
            addGameVersion("Java 17")
            addGameVersion("Fabric")
            addGameVersion("Quilt")
            mainArtifact(tasks.remapJar.get())
            relations(closureOf<CurseRelation> {
                requiredDependency("fabric-api")
                requiredDependency("fabric-language-kotlin")
            })
        })
        options(closureOf<Options> {
            forgeGradleIntegration = false
        })
    }

    afterEvaluate {
        tasks.getByName("curseforge639871").dependsOn(tasks.remapJar.get())
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = mavenGroup
            artifactId = project.name
            version = modVersion

            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(System.getenv("PUBLISH_REPO"))
            }
        }
    }
}
