repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:observability"))

    testImplementation(project(":core:support:tests"))
}
