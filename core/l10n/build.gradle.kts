repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    implementation(project(":core:pcbridge-api"))

    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
    testImplementation("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}
