import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.util.Properties

val generatedResourcesDir = "$buildDir/generated-resources"

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
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

    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    maven { url = uri("https://nexus.hc.to/content/repositories/pub_releases") }
    maven { url = uri("https://repo.aikar.co/content/groups/aikar/") } // idb-core
}

dependencies {
    implementation(project(":pcbridge-core"))
    implementation(project(":pcbridge-http"))
    implementation(project(":pcbridge-web-server"))

    compileOnly("org.spigotmc:spigot-api:1.19.2-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.4")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.essentialsx:EssentialsX:2.19.0")
    compileOnly("us.dynmap:dynmap-api:3.3")
    compileOnly("us.dynmap:DynmapCoreAPI:3.3")
    compileOnly(files("$projectDir/libs/GadgetsMenu.jar"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.11.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.12.1")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("io.sentry:sentry:5.7.4")
    implementation("dev.jorel:commandapi-bukkit-shade:9.0.3")

    testImplementation("net.md-5:bungeecord-api:1.16-R0.4")
    testImplementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

sourceSets {
    main {
        kotlin {
            // Bundle generated resources with the output jar
            output.dir(generatedResourcesDir)
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.withType<ShadowJar> {
    destinationDirectory.set(File("build/release"))
    archiveVersion.set(project.version.toString())

    // Prevent clashes with other Spigot plugins that may be shadowing CommandAPI
    relocate("dev.jorel.commandapi", "com.projectcitybuild.pcbridge.commandapi")
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
        val propertiesFile = file("$generatedResourcesDir/version.properties")
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
