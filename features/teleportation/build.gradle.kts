plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))
    implementation(project(":core:utils"))
    implementation(project(":core:support:tests"))
    implementation(project(":core:validation"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
