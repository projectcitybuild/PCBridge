import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val sharedGroup = "com.projectcitybuild.pcbridge"
val sharedVersion = "6.0.0"

group = sharedGroup
version = sharedVersion

plugins {
    kotlin("jvm") version "1.9.22"

    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id("co.uzzu.dotenv.gradle") version "4.0.0"
}

repositories {
    mavenCentral()
}

subprojects {
    group = sharedGroup
    version = sharedVersion

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")

    dependencies {
        // Kotlin
        implementation(kotlin("stdlib-jdk8"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

        // Tests
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
        testImplementation("org.mockito.kotlin:mockito-kotlin:5.0.0")
        testImplementation("org.mockito:mockito-inline:4.2.0")
    }

    sourceSets {
        test {
            java {
                srcDir("test")
                resources {
                    srcDir("test/resources")
                }
            }
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}
