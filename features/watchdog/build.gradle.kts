plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:datetime"))
    implementation(project(":core:discord"))
    implementation(project(":features:building"))
    implementation(project(":runtime"))
}
