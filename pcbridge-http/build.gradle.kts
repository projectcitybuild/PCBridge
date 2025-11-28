repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    implementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0:2.22.0-alpha")
}
