plugins {
    id("pcbridge.feature-conventions")
}

dependencies {
    implementation(project(":core:date-time"))
    implementation(project(":core:discord"))
    implementation(project(":core:l10n"))
    implementation(project(":features:building"))
    implementation(project(":runtime"))
}
