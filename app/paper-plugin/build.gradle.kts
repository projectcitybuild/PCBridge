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
    implementation(project(":core:http"))
    implementation(project(":core:pcbridge-api"))
    implementation(project(":platform:web-server"))

    // Core
    implementation(project(":core:observability"))
    implementation(project(":core:utils"))
    implementation(project(":core:storage"))
    implementation(project(":core:localconfig"))
    implementation(project(":core:discord"))
    implementation(project(":core:datetime"))
    implementation(project(":core:pagination"))
    implementation(project(":core:support:component"))
    implementation(project(":core:support:java"))
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))

    // Platform
    implementation(project(":platform:paper"))

    // Runtime
    implementation(project(":runtime"))

    // Paper
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation(project(":core:test-support"))

    // Integrations
    implementation(project(":integrations:dynmap"))
    implementation(project(":integrations:luckperms"))
    implementation(project(":integrations:essentials"))

    // Features
    implementation(project(":features:announcements"))
    implementation(project(":features:bans"))
    implementation(project(":features:building"))
    implementation(project(":features:builds"))
    implementation(project(":features:chatbadge"))
    implementation(project(":features:chatformatting"))
    implementation(project(":features:config"))
    implementation(project(":features:homes"))
    implementation(project(":features:maintenance"))
    implementation(project(":features:moderate"))
    implementation(project(":features:onboarding"))
    implementation(project(":features:pim"))
    implementation(project(":features:randomteleport"))
    implementation(project(":features:register"))
    implementation(project(":features:roles"))
    implementation(project(":features:serverlinks"))
    implementation(project(":features:spawns"))
    implementation(project(":features:staffchat"))
    implementation(project(":features:stats"))
    implementation(project(":features:sync"))
    implementation(project(":features:warnings"))
    implementation(project(":features:warps"))
    implementation(project(":features:watchdog"))
    implementation(project(":features:workstations"))

    // Libraries
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("io.sentry:sentry:8.27.1")
    implementation("io.sentry:sentry-opentelemetry-agentless:8.27.1")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.43.0")
    implementation("io.klogging:klogging:0.11.6")
    implementation("io.insert-koin:koin-core:3.5.6")
    implementation("io.github.reactivecircus.cache4k:cache4k:0.13.0")
    implementation("io.github.petertrr:kotlin-multiplatform-diff:0.7.0")
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
        minecraftVersion("1.21.10")
        systemProperty("com.mojang.eula.agree", "true")
        downloadPlugins {
            modrinth("LuckPerms", "v5.5.17-bukkit")
            modrinth("essentialsx", "2.21.2")

            // TODO: re-enable once it supports Paper 1.21.8
            // modrinth("dynmap", "3.7-beta-8")
        }
    }
}
