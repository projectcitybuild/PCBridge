plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:permissions"))
    implementation(project(":features:worlds"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
