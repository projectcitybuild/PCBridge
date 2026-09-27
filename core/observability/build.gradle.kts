repositories {
    mavenCentral()
}

dependencies {
    api("io.klogging:klogging:0.11.6")
    implementation("io.sentry:sentry:8.27.1")
    implementation("io.sentry:sentry-opentelemetry-agentless:8.27.1")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.43.0")
}
