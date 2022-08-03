
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.Properties

val generatedVersionDir = "$buildDir/generated-resources"

group = "com.projectcitybuild"
version = "4.1.1"

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"

    // klint
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"

    // klint integration with Idea
    // https://github.com/jlleitschuh/ktlint-gradle#additional-helper-tasks
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"
}

apply(plugin = "com.github.johnrengelman.shadow")

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven {
        name = "dynmap"
        url = uri("https://repo.mikeprimm.com/")
    }
    maven {
        name = "essentialsx-releases"
        url = uri("https://repo.essentialsx.net/releases/")
    }
    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    // For JsonConfiguration - delete later
    maven {
        name = "onarandombox"
        url = uri("https://repo.onarandombox.com/content/groups/public")
    }

    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://nexus.hc.to/content/repositories/pub_releases") }
    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") } // idb-core
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-native-mt")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-native-mt")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    compileOnly("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")
    annotationProcessor("org.spigotmc:plugin-annotations:1.2.3-SNAPSHOT")

    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")

    compileOnly("net.luckperms:api:5.4")

    compileOnly("net.essentialsx:EssentialsX:2.19.0")

    // dynmap
    compileOnly("us.dynmap:dynmap-api:3.3")
    compileOnly("us.dynmap:DynmapCoreAPI:3.3")

    // GadgetsMenu
    compileOnly(files("libs/GadgetsMenu.jar"))

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.2.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.2.0")

    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")

    implementation("com.google.dagger:dagger:2.42")
    kapt("com.google.dagger:dagger-compiler:2.42")

    implementation("io.sentry:sentry:5.7.4")

    implementation("redis.clients:jedis:4.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation("org.mockito:mockito-inline:4.2.0")

    testImplementation("net.md-5:bungeecord-api:1.16-R0.4") // Needed for mocking in tests
    testImplementation("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT") // Needed for mocking in tests

    // NBT reader for inventory importing
    implementation("br.com.gamemods:nbt-manipulator:3.1.0")

    // JSON serializer to match Multiverse-Inventory
    implementation("com.dumptruckman.minecraft:JsonConfiguration:1.1")
    implementation("net.minidev:json-smart:1.1.1")
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
        kotlin {
            output.dir(generatedVersionDir)
        }
    }
    test {
        java {
            srcDirs("src/test")
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
tasks.withType<ShadowJar> {
    destinationDirectory.set(File("build/release"))
    archiveVersion.set(project.version.toString())
}

tasks.test {
    useJUnitPlatform()
}

tasks.create("incrementVersion") {
    group = "automation"
    description = "Increments the output plugin version"

    fun generateVersion(): String {
        val updateMode = properties["mode"] ?: "minor"
        val currentVersion = version.toString()
        val (oldMajor, oldMinor, oldPatch) = currentVersion.split(".").map(String::toInt)
        var (newMajor, newMinor, newPatch) = arrayOf(oldMajor, oldMinor, 0)
        when (updateMode) {
            "major" -> newMajor = (oldMajor + 1).also { newMinor = 0 }
            "minor" -> newMinor = oldMinor + 1
            else -> newPatch = oldPatch + 1
        }
        return "$newMajor.$newMinor.$newPatch"
    }
    doLast {
        val newVersion = properties["overrideVersion"] as String? ?: generateVersion()
        val oldContent = buildFile.readText()
        val newContent = oldContent.replace("""= "$version"""", """= "$newVersion"""")
        buildFile.writeText(newContent)
    }
}

tasks.create("generateVersionResource") {
    group = "automation"
    description = "Generates a file containing the version that the plugin can access during runtime"

    val gitDescribe: String by lazy {
        val stdout = ByteArrayOutputStream()
        rootProject.exec {
            commandLine("git", "describe", "--tags")
            standardOutput = stdout
        }
        stdout.toString().trim()
            .replace("-g", "-") // Remove `g` for `git` in the commit id
    }
    doLast {
        val propertiesFile = file("$generatedVersionDir/version.properties")
        propertiesFile.parentFile.mkdirs()
        val properties = Properties().apply {
            setProperty("version", version.toString())
            setProperty("commit", gitDescribe)
        }
        val output = FileOutputStream(propertiesFile)
        properties.store(output, null)
    }
}

tasks.named("processResources") {
    dependsOn("generateVersionResource")
}
