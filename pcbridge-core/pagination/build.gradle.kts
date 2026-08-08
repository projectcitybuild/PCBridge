plugins {
    kotlin("jvm")
}

group = "com.projectcitybuild.pcbridge"
version = "6.10.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}