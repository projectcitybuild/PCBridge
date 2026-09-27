/**
 * Shared conventions for `features:*` subprojects: common repositories, the
 * baseline runtime libraries every feature needs, and the Paper API as a
 * compile-only dependency.
 *
 * Applied via `plugins { id("pcbridge.feature-conventions") }` in each
 * feature's build.gradle.kts. Project-specific dependencies (which
 * `core:*`/`platform:*`/`runtime:*`/`integrations:*`/`features:*` modules a
 * given feature needs) are declared explicitly in that feature's own
 * `dependencies {}` block, not here.
 */
repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    add("implementation", project(":core:pcbridge-api"))
    add("implementation", project(":core:observability"))
    add("implementation", "com.github.shynixn.mccoroutine:mccoroutine-bukkit-api:2.15.0")
    add("implementation", "io.insert-koin:koin-core:3.5.6")
    add("implementation", "io.github.reactivecircus.cache4k:cache4k:0.13.0")
    add("implementation", "io.github.petertrr:kotlin-multiplatform-diff:0.7.0")
    add("compileOnly", "io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")
}
