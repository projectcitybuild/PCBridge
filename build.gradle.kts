import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()

        maven {
            name = "dynmap"
            url = uri("https://repo.mikeprimm.com/")
        }
    }
}

plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"
}

apply(plugin = "com.github.johnrengelman.shadow")

repositories {
    mavenCentral()

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

    compileOnly("net.luckperms:api:5.3")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:1.5.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:1.5.0")

    implementation("com.zaxxer:HikariCP:5.0.1")

    implementation("co.aikar:idb-core:1.0.0-SNAPSHOT")

    implementation("com.google.dagger:dagger:2.40.5")
    kapt("com.google.dagger:dagger-compiler:2.40.5")

    implementation("io.sentry:sentry:5.5.2")

    implementation("redis.clients:jedis:4.0.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")

    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.powermock:powermock-module-junit4:2.0.9")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
    testImplementation("org.mockito:mockito-inline:4.2.0")

    testImplementation ("net.md-5:bungeecord-api:1.16-R0.4") // Needed for mocking in tests
    testImplementation ("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT") // Needed for mocking in tests

    // dynmap
    compileOnly ("us.dynmap:dynmap-api:3.3")
    compileOnly ("us.dynmap:DynmapCoreAPI:3.3")

    // Spigot
    compileOnly ("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")

    // Bungeecord
    compileOnly ("net.md-5:bungeecord-api:1.16-R0.4")
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
    archiveBaseName.set("all")
    destinationDirectory.set(File("build/release"))
    archiveVersion.set("3.6.0")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets {
    main {
        java {
            srcDirs("src/main/kotlin")
        }
    }
    test {
        java {
            srcDirs("src/test")
        }
    }
}

//task createProperties() {
//    doLast {
//        def details = versionDetails()
//        new File("$buildDir/resources/main/version.properties").withWriter { w ->
//            Properties p = new Properties()
//            p["version"] = project.version.toString()
//            p["gitLastTag"] = details.lastTag
//            p["gitCommitDistance"] = details.commitDistance.toString()
//            p["gitHash"] = details.gitHash.toString()
//            p["gitHashFull"] = details.gitHashFull.toString() // full 40-character Git commit hash
//            p["gitBranchName"] = details.branchName // is null if the repository in detached HEAD mode
//            p["gitIsCleanTag"] = details.isCleanTag.toString()
//            p.store w, null
//        }
//        // copy needed, otherwise the bean VersionController can"t load the file at startup when running complete-app tests.
//        copy {
//            from "$buildDir/resources/main/version.properties"
//            into "bin/main/"
//        }
//    }
//}