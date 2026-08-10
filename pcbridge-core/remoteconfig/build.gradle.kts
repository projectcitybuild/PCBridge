plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-http"))
    implementation(project(":pcbridge-observability"))
    implementation(project(":pcbridge-core:storage"))
}