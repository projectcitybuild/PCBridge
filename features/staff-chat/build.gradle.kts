plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))

    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}
