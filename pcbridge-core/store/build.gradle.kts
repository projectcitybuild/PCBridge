plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-core:datetime"))
    implementation(project(":pcbridge-core:storage"))
    implementation(project(":pcbridge-observability"))
    implementation(project(":pcbridge-http"))
}