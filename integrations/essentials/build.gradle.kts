repositories {
    mavenCentral()
    maven {
        name = "essentialsx-releases"
        url = uri("https://repo.essentialsx.net/releases/")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":core:datetime"))
    implementation(project(":core:observability"))
    implementation(project(":platform:paper"))
    implementation(project(":runtime"))
    implementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    implementation("io.insert-koin:koin-core:3.5.6")

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    compileOnly("net.essentialsx:EssentialsX:2.21.2")
}
