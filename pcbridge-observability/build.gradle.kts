plugins {
    kotlin("jvm")
}

dependencies {
    implementation("io.sentry:sentry:8.27.1")
    implementation("io.sentry:sentry-opentelemetry-agentless:8.27.1")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.43.0")
    implementation("io.klogging:klogging:0.11.6")

    // HTTP tracing
    implementation("io.opentelemetry.instrumentation:opentelemetry-okhttp-3.0:2.22.0-alpha")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}