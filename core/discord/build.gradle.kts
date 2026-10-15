repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:pcbridge-api"))
    implementation(project(":core:local-config"))
    implementation(project(":core:observability"))
    implementation(project(":core:utils"))
}
