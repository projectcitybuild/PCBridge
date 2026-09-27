plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:l10n"))
    implementation(project(":core:utils"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
}
