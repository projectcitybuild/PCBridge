plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-core:localconfig"))
    implementation(project(":pcbridge-core:observability"))
    implementation(project(":pcbridge-http"))
}