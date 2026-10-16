plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
    maven {
        name = "essentialsx-releases"
        url = uri("https://repo.essentialsx.net/releases/")
    }
}

dependencies {
    implementation(project(":core:date-time"))
    implementation(project(":core:observability"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")

    compileOnly("net.essentialsx:EssentialsX:2.21.2")
}
