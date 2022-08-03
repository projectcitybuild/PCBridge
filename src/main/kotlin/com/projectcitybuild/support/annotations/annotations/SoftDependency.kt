package com.projectcitybuild.support.annotations.annotations

/**
 * Represents a soft (optional) dependency for this plugin.
 * If this dependency is not present, the plugin will still load.
 * <br></br>
 * The **name** attribute of the plugin is required in order to specify the target. <br></br>
 * Circular soft-dependencies are loaded arbitrarily.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Repeatable
annotation class SoftDependency(
    /**
     * A plugin that is required in order for this plugin to have full functionality.
     */
    val value: String
)
