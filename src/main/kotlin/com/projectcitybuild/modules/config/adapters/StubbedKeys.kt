package com.projectcitybuild.modules.config.adapters

import com.projectcitybuild.modules.config.ConfigKeys

class StubbedKeys: ConfigKeys {

    override var API_ENABLED: Boolean = null!!

    override val API_KEY: String = null!!

    override val API_TOKEN: String = null!!

    override val API_BASE_URL: String = null!!

    override val API_IS_LOGGING_ENABLED: Boolean = null!!


    override val DB_HOSTNAME: String = null!!

    override val DB_PORT: Int = null!!

    override val DB_NAME: String = null!!

    override val DB_USERNAME: String = null!!

    override val DB_PASSWORD: String = null!!


    override val ERROR_REPORTING_SENTRY_ENABLED: Boolean = null!!

    override val ERROR_REPORTING_SENTRY_DSN: String = null!!


    override val TIME_TIMEZONE: String = null!!

    override val TIME_LOCALE: String = null!!


    override val WARPS_PER_PAGE: Int = null!!


    override val INTEGRATION_DYNMAP_WARP_ICON: String = null!!


    override val GROUPS_GUEST: String = null!!

    override val GROUPS_BUILD_PRIORITY: List<String> = null!!

    override val GROUPS_TRUST_PRIORITY: List<String> = null!!

    override val GROUPS_DONOR_PRIORITY: List<String> = null!!
}