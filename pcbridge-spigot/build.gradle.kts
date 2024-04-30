import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

val generatedResourcesDir = "${layout.buildDirectory}/generated-resources"

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

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

    // Spigot
    compileOnly("org.spigotmc:spigot-api:1.20.4-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.20-R0.1")

    // Integrations
    // compileOnly("net.luckperms:api:5.4")
    // compileOnly("net.essentialsx:EssentialsX:2.19.0")
    // compileOnly("us.dynmap:dynmap-api:3.3")
    // compileOnly("us.dynmap:DynmapCoreAPI:3.3")

    // Libraries
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")
    implementation("io.sentry:sentry:5.7.4")
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")

    // Testing
    // testImplementation("net.md-5:bungeecord-api:1.16-R0.4")
    // testImplementation("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
}

sourceSets {
    main {
        kotlin {
            // TODO: is this still needed?
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
    destinationDirectory.set(File(
        env.fetchOrNull("BUILD_OUTPUT_DIR") ?: "build/release"
    ))
    archiveVersion.set(project.version.toString())
    this.dependsOn
}