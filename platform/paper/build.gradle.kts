plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:observability"))
    implementation(project(":core:utils"))
    implementation(project(":core:permissions"))
    implementation(project(":core:l10n"))

    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-core:2.15.0")

    testImplementation(project(":core:support:tests"))
}
