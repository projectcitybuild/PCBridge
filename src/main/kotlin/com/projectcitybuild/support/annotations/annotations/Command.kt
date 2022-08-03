package com.projectcitybuild.support.annotations.annotations

/**
 * Defines a plugin command
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@JvmRepeatable(Commands::class)
annotation class Command(
    /**
     * This command's name.
     */
    val name: String,
    /**
     * This command's description.
     */
    val desc: String = "",
    /**
     * This command's aliases.
     */
    val aliases: Array<String> = [],
    /**
     * This command's permission node.
     */
    val permission: String = "",
    /**
     * This command's permission-check-fail message.
     */
    val permissionMessage: String = "",
    /**
     * This command's usage message.
     */
    val usage: String = ""
)
