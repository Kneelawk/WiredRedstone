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

val modId: String by project

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

val genResDir = file("src/main/resources-generated")

loom {
    accessWidenerPath.set(file("src/main/resources/wiredredstone.accesswidener"))

//    enableTransitiveAccessWideners.set(false)

    runs {
        getByName("client") {
            programArgs("--width", "1920", "--height", "1080")
        }

        create("datagen") {
            inherit(getByName("client"))
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${genResDir}")
            vmArg("-Dfabric-api.datagen.modid=${modId}")

            runDir("build/datagen")
        }
    }
}

sourceSets {
    named("main") {
        val emiEnabled: String by project
        val reiEnabled: String by project
        val createEnabled: String by project
        val ccEnabled: String by project
        val wthitEnabled: String by project
        kotlin {
            if (!emiEnabled.toBoolean()) {
                exclude("com/kneelawk/wiredredstone/compat/emi/impl/**")
            }

            if (!reiEnabled.toBoolean()) {
                exclude("com/kneelawk/wiredredstone/compat/rei/impl/**")
            }

            if (!createEnabled.toBoolean()) {
                exclude("com/kneelawk/wiredredstone/compat/create/impl/**")
            }

            if (!ccEnabled.toBoolean()) {
                exclude("com/kneelawk/wiredredstone/compat/cc/impl/**")
            }

            if (!wthitEnabled.toBoolean()) {
                exclude("com/kneelawk/wiredredstone/compat/wthit/impl/**")
            }
        }
        resources {
            srcDir(genResDir)
            if (!wthitEnabled.toBoolean()) {
                exclude("waila_plugins.json")
            }
        }
    }
}

repositories {
    maven("https://mod-buildcraft.com/maven") { name = "BuildCraft" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://maven.quiltmc.org/repository/release") { name = "QuiltMC" }
    maven("https://maven.vram.io/") { name = "VRAM" }
    maven("https://maven.shedaniel.me/") { name = "shedaniel" }
    maven("https://kneelawk.com/maven/") { name = "Kneelawk" }
    maven("https://maven2.bai.lol") { name = "WTHIT" }
    maven("https://squiddev.cc/maven") {
        name = "SquidDev"
        content {
            includeGroup("cc.tweaked")
            includeModule("org.squiddev", "Cobalt")
        }
    }
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
    maven("https://maven.ladysnake.org/releases") { name = "Ladysnake" }
    maven("https://maven.tterrag.com/") { name = "Flywheel" }
//    maven("https://maven.cafeteria.dev/releases/") { name = "Cafeteria" } // for Fake Player API
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
//    val lmpCompatVersion: String by project
//    modImplementation("com.kneelawk:lmp-compat:$lmpCompatVersion")
//    include("com.kneelawk:lmp-compat:$lmpCompatVersion")

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

    //
    // Optional Compile-Time Dependencies
    //

    // WTHIT API
    val wthitEnabled: String by project
    val wthitVersion: String by project
    if (wthitEnabled.toBoolean()) {
        modCompileOnly("mcp.mobius.waila:wthit-api:fabric-$wthitVersion")
    }

    // CC: Tweaked
    val ccEnabled: String by project
    val ccVersion: String by project
    val ccMCVersion: String by project
    if (ccEnabled.toBoolean()) {
        modCompileOnly("cc.tweaked:cc-tweaked-$ccMCVersion-fabric-api:$ccVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
    }

    // REI
    val reiEnabled: String by project
    val reiVersion: String by project
    if (reiEnabled.toBoolean()) {
        modCompileOnly("me.shedaniel:RoughlyEnoughItems-api-fabric:$reiVersion")
        modCompileOnly("me.shedaniel:RoughlyEnoughItems-default-plugin-fabric:$reiVersion")
    }

    // EMI
    val emiEnabled: String by project
    val emiVersion: String by project
    if (emiEnabled.toBoolean()) {
        modCompileOnly("dev.emi:emi-fabric:$emiVersion") {
            isTransitive = false
        }
    }

    // Create
    val createEnabled: String by project
    val createVersion: String by project
    val createMinecraftVersion: String by project
    if (createEnabled.toBoolean()) {
        modCompileOnly("com.simibubi.create:create-fabric-$createMinecraftVersion:$createVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
    }

    //
    // Runtime Dependencies
    //

    // Mod Menu
    val modMenuEnabled: String by project
    val modMenuVersion: String by project
    if (modMenuEnabled.toBoolean()) {
        modLocalRuntime("com.terraformersmc:modmenu:$modMenuVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
    }

    // WTHIT
    if (wthitEnabled.toBoolean()) {
        modLocalRuntime("mcp.mobius.waila:wthit:fabric-$wthitVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
    }

    // CC: Tweaked
    if (ccEnabled.toBoolean()) {
        modLocalRuntime("cc.tweaked:cc-tweaked-$ccMCVersion-fabric:$ccVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
    }

    // Recipe viewers
    val runtimeViewer: String by project
    when {
        runtimeViewer == "emi" && emiEnabled.toBoolean() -> {
            modLocalRuntime("dev.emi:emi-fabric:$emiVersion") {
                isTransitive = false
            }
        }
        runtimeViewer == "rei" && reiEnabled.toBoolean() -> {
            modLocalRuntime("me.shedaniel:RoughlyEnoughItems-fabric:$reiVersion") {
                exclude("net.fabricmc.fabric-api")
            }
        }
    }


    // Create
    if (createEnabled.toBoolean()) {
        modLocalRuntime("com.simibubi.create:create-fabric-$createMinecraftVersion:$createVersion") {
            exclude("net.fabricmc")
            exclude("net.fabricmc.fabric-api")
        }
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
        val emiEnabled: String by project
        val emiKey = if (emiEnabled.toBoolean()) "emi" else "emi_disabled"
        val reiEnabled: String by project
        val reiClientKey = if (reiEnabled.toBoolean()) "rei_client" else "rei_client_disabled"
        val reiCommonKey = if (reiEnabled.toBoolean()) "rei_common" else "rei_common_disabled"

        inputs.property("version", project.version)
        inputs.property("emiEnabled", emiEnabled)
        inputs.property("reiEnabled", reiEnabled)

        exclude("**/*.xcf")
        exclude("**/textures/**/*.txt")
        exclude("**/textures/**/*.gpl")

        filesMatching("fabric.mod.json") {
            expand(
                mutableMapOf(
                    "version" to project.version, "emi_key" to emiKey, "rei_client_key" to reiClientKey,
                    "rei_common_key" to reiCommonKey
                )
            )
        }
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

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithQuiltflower"))
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
