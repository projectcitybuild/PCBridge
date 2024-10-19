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

    implementation("io.ktor:ktor-server-core:2.2.4")
    implementation("io.ktor:ktor-server-netty:2.2.4")
    implementation("io.ktor:ktor-server-call-logging:2.2.4")
    implementation("io.ktor:ktor-server-auth:2.2.4")
}