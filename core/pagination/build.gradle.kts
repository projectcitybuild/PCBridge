plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:pcbridge-api"))
    implementation(project(":core:l10n"))

}
