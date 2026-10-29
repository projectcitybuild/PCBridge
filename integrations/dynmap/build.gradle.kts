plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
    maven {
        name = "dynmap"
        url = uri("https://repo.mikeprimm.com/")
    }
}

dependencies {
    implementation(project(":core:observability"))
    implementation(project(":features:spawns"))
    implementation(project(":features:warps"))
    implementation(project(":features:worlds"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")

    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")

    testImplementation(project(":core:support:tests"))
    testImplementation("us.dynmap:DynmapCoreAPI:3.7-beta-6")
}
