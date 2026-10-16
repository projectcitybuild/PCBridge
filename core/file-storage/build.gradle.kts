plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:observability"))
}
