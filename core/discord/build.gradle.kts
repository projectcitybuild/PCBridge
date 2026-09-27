repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:pcbridge-api"))
    implementation(project(":core:localconfig"))
    implementation(project(":core:observability"))
    implementation(project(":core:utils"))
}
