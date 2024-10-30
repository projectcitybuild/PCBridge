import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
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
}

dependencies {
    implementation(project(":pcbridge-http"))
    implementation(project(":pcbridge-web-server"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")

    // Integrations
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("us.dynmap:dynmap-api:3.3")
    compileOnly("us.dynmap:DynmapCoreAPI:3.3")

    // Libraries
    implementation(kotlin("reflect"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("io.sentry:sentry:5.7.4")
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
    implementation("net.kyori:adventure-platform-bukkit:4.3.2")
    implementation("net.kyori:adventure-text-minimessage:4.16.0")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")

    // Testing
    testImplementation("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

sourceSets {
    main {
        kotlin {
            // TODO: is this still needed?
            // Bundle generated resources with the output jar
            output.dir("${layout.buildDirectory}/generated-resources")
        }
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        // Outputs the JAR to a location specified in the .env file if present.
        //
        // Useful for faster testing, since we can output the JAR directly to the "plugins"
        // folder if you wish to manually boot up a server yourself
        destinationDirectory.set(
            File(env.fetchOrNull("BUILD_OUTPUT_DIR") ?: "build/release"),
        )
        archiveVersion.set(project.version.toString())
    }
    runServer {
        minecraftVersion("1.21.1")

        systemProperty("com.mojang.eula.agree", "true")

        downloadPlugins {
            url("https://download.luckperms.net/1560/bukkit/loader/LuckPerms-Bukkit-5.4.145.jar")
            url("https://cdn.modrinth.com/data/fRQREgAc/versions/AdtrWcU2/Dynmap-3.7-beta-7-spigot.jar")
            github("EssentialsX", "Essentials", "2.20.1", "EssentialsX-2.20.1.jar")
        }
    }
}