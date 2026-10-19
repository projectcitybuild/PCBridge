plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:l10n"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
