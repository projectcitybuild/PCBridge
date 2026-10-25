import pcbridge.tasks.GenerateE2ePermissionNodesTask

val generateE2ePermissionNodes =
    tasks.register<GenerateE2ePermissionNodesTask>("generateE2ePermissionNodes") {
        group = "verification"
        description = "Generates TypeScript constants for all Kotlin permission nodes"

        permissionSources.from(
            rootProject.allprojects.map { subproject ->
                subproject.fileTree("src/main/kotlin") {
                    include("**/*.kt")
                }
            },
        )
        outputFile.set(layout.projectDirectory.file("src/test/e2e/tests/helpers/permission-nodes.generated.ts"))
    }

tasks.matching { it.name == "plugwrightCompileTests" }.configureEach {
    dependsOn(generateE2ePermissionNodes)
}
