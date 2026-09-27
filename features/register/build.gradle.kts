plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:http"))
    implementation(project(":core:l10n"))
    implementation(project(":features:sync"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
}
