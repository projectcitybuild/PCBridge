plugins {
    application
}

application {
    mainClass.set("com.projectcitybuild.pcbridge.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":pcbridge-http"))

    implementation("io.ktor:ktor-server-core:3.0.0")
    implementation("io.ktor:ktor-server-netty:3.0.0")
    implementation("io.ktor:ktor-server-call-logging:3.0.0")
    implementation("io.ktor:ktor-server-auth:3.0.0")
    implementation("io.ktor:ktor-server-content-negotiation:3.0.0")
    implementation("io.ktor:ktor-serialization-gson:3.0.0")
}