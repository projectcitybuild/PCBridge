plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:observability"))
    implementation(project(":runtime"))
    implementation("io.insert-koin:koin-core:3.5.6")
    compileOnly("net.luckperms:api:5.4")
}
