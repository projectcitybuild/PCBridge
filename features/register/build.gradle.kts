plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:http"))
    implementation(project(":features:sync"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
