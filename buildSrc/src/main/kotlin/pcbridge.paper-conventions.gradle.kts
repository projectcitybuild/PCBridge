/**
 * Shared Paper API repository and dependencies for projects that compile
 * against Paper.
 */
val paperApiDependency = "io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT"

repositories {
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    add("compileOnly", paperApiDependency)
    add("testImplementation", paperApiDependency)
}
