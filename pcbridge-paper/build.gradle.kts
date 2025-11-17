plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
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
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":pcbridge-http"))
    implementation(project(":pcbridge-web-server"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")

    // Integrations
    compileOnly("net.luckperms:api:5.4")
    compileOnly("net.essentialsx:EssentialsX:2.20.1")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")

    // Libraries
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("io.sentry:sentry:8.26.0")
    implementation("io.sentry:sentry-kotlin-extensions:8.26.0")
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
    implementation("io.klogging:klogging:0.11.6")
    implementation("io.github.petertrr:kotlin-multiplatform-diff:0.7.0")

    // Testing
    testImplementation("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")
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
        minecraftVersion("1.21.3")
        systemProperty("com.mojang.eula.agree", "true")
        downloadPlugins {
            modrinth("LuckPerms", "v5.5.17-bukkit")
            modrinth("essentialsx", "2.21.2")
            modrinth("dynmap", "3.7-beta-8")
        }
    }
}