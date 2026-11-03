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

tasks.named<Test>("test") {
    systemProperty("stringsEnSource", stringsEnSource.absolutePath)
}

val testSourceSet = sourceSets.named("test")
val auditReport = layout.buildDirectory.file("reports/l10n-audit/StringsEn-audit.txt")

tasks.register<JavaExec>("auditL10n") {
    description = "Audits localization placeholders and MiniMessage usage."
    group = "verification"
    dependsOn(tasks.named("testClasses"))
    classpath = testSourceSet.get().runtimeClasspath
    mainClass.set("com.projectcitybuild.pcbridge.core.l10n.StringsEnAuditCli")
    args(stringsEnSource.absolutePath, auditReport.get().asFile.absolutePath)
    inputs.file(stringsEnSource)
    outputs.file(auditReport)
}
