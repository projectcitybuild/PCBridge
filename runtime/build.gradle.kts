repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":core:http"))
    api(project(":core:pcbridge-api"))
    implementation(project(":core:observability"))
    implementation(project(":core:datetime"))
    implementation(project(":core:storage"))
    implementation(project(":core:utils"))
    implementation(project(":core:permissions"))
    implementation(project(":core:support:java"))
    implementation(project(":core:support:component"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation(project(":core:test-support"))

    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.43.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")
}
