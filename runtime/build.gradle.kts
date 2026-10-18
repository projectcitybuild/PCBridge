plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:http"))
    api(project(":core:pcbridge-api"))
    implementation(project(":core:observability"))
    implementation(project(":core:date-time"))
    implementation(project(":core:file-storage"))
    implementation(project(":core:l10n"))
    implementation(project(":core:utils"))
    implementation(project(":core:permissions"))
    implementation(project(":core:support:java"))
    implementation(project(":core:support:component"))
    implementation(project(":platform:paper"))
    implementation(project(":platform:web-server"))

    testImplementation(project(":core:support:tests"))

    implementation("io.opentelemetry:opentelemetry-extension-kotlin:1.43.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")
}
