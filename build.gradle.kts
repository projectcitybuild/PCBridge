import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "com.projectcitybuild.pcbridge"
version = "5.0.0"

plugins {
    kotlin("jvm") version "1.6.10"

    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-native-mt")
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
            jvmTarget = "11"
        }
    }

    tasks.test {
        useJUnitPlatform()
    }
}
