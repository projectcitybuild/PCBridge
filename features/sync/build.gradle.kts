plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:datetime"))
    implementation(project(":core:permissions"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))
    implementation(project(":runtime"))
}
