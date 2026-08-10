plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-core:observability"))
    implementation(project(":pcbridge-http"))
    implementation(project(":pcbridge-shared"))
}