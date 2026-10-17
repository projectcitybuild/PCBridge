plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:date-time"))
    implementation(project(":runtime"))

    testImplementation(project(":core:test-support"))
}
