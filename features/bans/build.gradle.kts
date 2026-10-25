plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:http"))
    implementation(project(":core:permissions"))
    implementation(project(":core:support:component"))
    implementation(project(":core:support:java"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
