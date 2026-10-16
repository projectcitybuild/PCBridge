plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:observability"))

    testImplementation(project(":core:test-support"))
}
