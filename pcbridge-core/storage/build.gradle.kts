plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":pcbridge-core:observability"))

    implementation("com.google.code.gson:gson:2.13.1")
}