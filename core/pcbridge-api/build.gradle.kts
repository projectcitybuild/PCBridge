repositories {
    mavenCentral()
}

dependencies {
    api(project(":core:http"))

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
}
