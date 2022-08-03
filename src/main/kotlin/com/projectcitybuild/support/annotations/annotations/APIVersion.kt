package com.projectcitybuild.support.annotations.annotations

/**
 * This annotation specifies the api version of the plugin.
 * <br></br>
 * Defaults to [ApiVersion.Target.DEFAULT].
 * <br></br>
 * Pre-1.13 plugins do not need to use this annotation.
 */
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class ApiVersion(
    val value: Target = Target.DEFAULT
) {
    /**
     * Specifies the target api-version for this plugin.
     *
     * All pre-1.13 plugins must use [.DEFAULT].
     */
    enum class Target(val version: String?) {
        /**
         * This target version specifies that the plugin was made for pre-1.13 Spigot versions.
         */
        DEFAULT(null),

        /**
         * This target version specifies that the plugin was made with 1.13+ versions in mind.
         */
        v1_13("1.13"),

        /**
         * This target version specifies that the plugin was made with 1.14+ versions in mind.
         */
        v1_14("1.14"),

        /**
         * This target version specifies that the plugin was made with 1.15+ versions in mind.
         */
        v1_15("1.15"),

        /**
         * This target version specifies that the plugin was made with 1.16+ versions in mind.
         */
        v1_16("1.16"),

        /**
         * This target version specifies that the plugin was made with 1.17+ versions in mind.
         */
        v1_17("1.17"),

        /**
         * This target version specifies that the plugin was made with 1.18+ versions in mind.
         */
        v1_18("1.18"),

        /**
         * This target version specifies that the plugin was made with 1.19+ versions in mind.
         */
        v1_19("1.19");
    }
}
