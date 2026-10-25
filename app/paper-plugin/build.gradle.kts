import me.drownek.plugwright.local.LocalMode

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.github.drownek.plugwright") version "3.0.0"
    id("pcbridge.paper-conventions")
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
}

dependencies {
    implementation(project(":core:http"))
    implementation(project(":core:pcbridge-api"))
    implementation(project(":platform:web-server"))

    // Core
    implementation(project(":core:observability"))
    implementation(project(":core:utils"))
    implementation(project(":core:file-storage"))
    implementation(project(":core:local-config"))
    implementation(project(":core:discord"))
    implementation(project(":core:date-time"))
    implementation(project(":core:pagination"))
    implementation(project(":core:support:component"))
    implementation(project(":core:support:java"))
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))

    // Platform
    implementation(project(":platform:paper"))

    // Runtime
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))

    // Integrations
    implementation(project(":integrations:dynmap"))
    implementation(project(":integrations:luckperms"))
    implementation(project(":integrations:essentials"))

    // Features
    implementation(project(":features:announcements"))
    implementation(project(":features:bans"))
    implementation(project(":features:building"))
    implementation(project(":features:builds"))
    implementation(project(":features:chat-badge"))
    implementation(project(":features:chat-formatting"))
    implementation(project(":features:config"))
    implementation(project(":features:homes"))
    implementation(project(":features:maintenance"))
    implementation(project(":features:moderate"))
    implementation(project(":features:onboarding"))
    implementation(project(":features:pim"))
    implementation(project(":features:register"))
    implementation(project(":features:roles"))
    implementation(project(":features:server-links"))
    implementation(project(":features:spawns"))
    implementation(project(":features:staff-chat"))
    implementation(project(":features:stats"))
    implementation(project(":features:sync"))
    implementation(project(":features:teleportation"))
    implementation(project(":features:warnings"))
    implementation(project(":features:warps"))
    implementation(project(":features:worlds"))
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

plugwright {
    testsDir.set(file("src/test/e2e"))
    downloadNode.set(false)

    environments {
        create("local", LocalMode) {
            minecraftVersion.set("1.21.10")
            acceptEula.set(true)

            plugins {
                local("reset-player-permissions")
            }

            downloadPlugins {
                url("https://cdn.modrinth.com/data/Vebnzrzj/versions/OrIs0S6b/LuckPerms-Bukkit-5.5.17.jar")
            }

            writeFiles {
                file(
                    "server.properties",
                    """
                    online-mode=false
                    enforce-secure-profile=false
                    """.trimIndent(),
                )
                file(
                    "plugins/PCBridge/config.json",
                    """
                    {
                      "environment": "DEV",
                      "api": {
                        "token": "pcbridge_local",
                        "base_url": "http://api.localhost/",
                        "log_level": "trace"
                      },
                      "web_server": {
                        "token": "pcbridge_e2e_local_only",
                        "host": "127.0.0.1",
                        "port": 8080
                      },
                      "observability": {
                        "sentry_dsn": "",
                        "trace_sample_rate": 1.0
                      },
                      "discord": {
                        "content_alert_webhook": ""
                      }
                    }
                    """.trimIndent(),
                )
            }
        }
    }
}
