plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-core:datetime"))
    implementation(project(":pcbridge-core:observability"))
    implementation(project(":pcbridge-core:storage"))
    implementation(project(":pcbridge-http"))
}