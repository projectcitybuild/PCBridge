plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    testImplementation(project(":core:test-support"))

    implementation(project(":core:l10n"))
    implementation(project(":core:pagination"))
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))
    implementation(project(":runtime"))
}
