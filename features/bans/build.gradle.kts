plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:http"))
    implementation(project(":core:l10n"))
    implementation(project(":core:permissions"))
    implementation(project(":core:support:component"))
    implementation(project(":core:support:java"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))
    implementation(project(":runtime"))

    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation(project(":core:test-support"))
}
