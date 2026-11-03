plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))

    testImplementation(project(":core:support:tests"))
}
