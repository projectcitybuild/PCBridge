plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
