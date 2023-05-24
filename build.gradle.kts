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

// tasks.create("incrementVersion") {
//     group = "automation"
//     description = "Increments the output plugin version"
//
//     fun generateVersion(): String {
//         val updateMode = properties["mode"] ?: "minor"
//         val currentVersion = version.toString()
//         val (oldMajor, oldMinor, oldPatch) = currentVersion.split(".").map(String::toInt)
//         var (newMajor, newMinor, newPatch) = arrayOf(oldMajor, oldMinor, 0)
//         when (updateMode) {
//             "major" -> newMajor = (oldMajor + 1).also { newMinor = 0 }
//             "minor" -> newMinor = oldMinor + 1
//             else -> newPatch = oldPatch + 1
//         }
//         return "$newMajor.$newMinor.$newPatch"
//     }
//     doLast {
//         val newVersion = properties["overrideVersion"] as String? ?: generateVersion()
//         val oldContent = buildFile.readText()
//         val newContent = oldContent.replace("""= "$version"""", """= "$newVersion"""")
//         buildFile.writeText(newContent)
//     }
// }
//
