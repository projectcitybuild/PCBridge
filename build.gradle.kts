import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"

    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
    id("org.jetbrains.kotlin.kapt") version "1.6.10"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
    id("org.jlleitschuh.gradle.ktlint-idea") version "10.2.1"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

        // Main
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.0-native-mt")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.0-native-mt")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")

        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
        testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
        testImplementation("org.powermock:powermock-module-junit4:2.0.9")
        testImplementation("org.powermock:powermock-api-mockito2:2.0.9")
        testImplementation("org.mockito:mockito-inline:4.2.0")
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
