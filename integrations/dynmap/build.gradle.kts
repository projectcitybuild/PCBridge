repositories {
    mavenCentral()
    maven {
        name = "dynmap"
        url = uri("https://repo.mikeprimm.com/")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":core:observability"))
    implementation(project(":features:spawns"))
    implementation(project(":features:warps"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("us.dynmap:DynmapCoreAPI:3.7-beta-6")
}
