package com.projectcitybuild.support.annotations.annotations

/**
 * Part of the plugin annotations framework.
 *
 *
 * Represents a list of this plugin's registered command(s).
 * <br></br>
 * This specific annotation should not be used by people who do not know
 * how repeating annotations work.
 */
@MustBeDocumented
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Commands(vararg val value: Command = [])
