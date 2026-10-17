plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:date-time"))
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))
    implementation(project(":runtime"))

    testImplementation(project(":core:test-support"))
}
