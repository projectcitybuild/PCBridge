plugins {
    id("pcbridge.paper-conventions")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core:pcbridge-api"))
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.0")
}

val stringsEnSource = layout.projectDirectory
    .file("src/main/kotlin/com/projectcitybuild/pcbridge/core/l10n/StringsEn.kt")
    .asFile

tasks.withType<Test>().configureEach {
    systemProperty("stringsEnSource", stringsEnSource.absolutePath)
}

val testSourceSet = sourceSets.named("test")

tasks.register<Test>("auditL10n") {
    description = "Audits localization placeholders and MiniMessage usage."
    group = "verification"
    testClassesDirs = testSourceSet.get().output.classesDirs
    classpath = testSourceSet.get().runtimeClasspath
    useJUnitPlatform()
    testLogging {
        showStandardStreams = true
    }
    filter {
        includeTestsMatching("com.projectcitybuild.pcbridge.core.l10n.StringsEnAuditTest")
    }
    reports.junitXml.outputLocation.set(layout.buildDirectory.dir("test-results/auditL10n"))
    reports.html.outputLocation.set(layout.buildDirectory.dir("reports/tests/auditL10n"))
}
