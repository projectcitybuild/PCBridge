plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    testImplementation(project(":core:support:tests"))

    implementation(project(":core:pagination"))
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
}
