plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:datetime"))
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))
    implementation(project(":core:support:java"))
    implementation(project(":core:utils"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
}
