package com.projectcitybuild.modules.config.adapters

import com.projectcitybuild.core.storage.Storage
import com.projectcitybuild.core.storage.defaultsTo
import com.projectcitybuild.modules.config.ConfigKeys

class StorageBackedKeys constructor(
    private val storage: Storage,
): ConfigKeys {

    override val API_ENABLED: Boolean
        get() = storage.get("api.enabled" defaultsTo super.API_ENABLED)

    override val API_KEY: String
        get() = storage.get("api.key" defaultsTo super.API_KEY)

    override val API_TOKEN: String
        get() = storage.get("api.token" defaultsTo super.API_TOKEN)

    override val API_BASE_URL: String
        get() = storage.get("api.base_url" defaultsTo super.API_BASE_URL)

    override val API_IS_LOGGING_ENABLED: Boolean
        get() = storage.get("api.is_logging_enabled" defaultsTo super.API_IS_LOGGING_ENABLED)


    override val DB_HOSTNAME: String
        get() = storage.get("database.hostname" defaultsTo super.DB_HOSTNAME)

    override val DB_PORT: Int
        get() = storage.get("database.port" defaultsTo super.DB_PORT)

    override val DB_NAME: String
        get() = storage.get("database.name" defaultsTo super.DB_NAME)

    override val DB_USERNAME: String
        get() = storage.get("database.username" defaultsTo super.DB_USERNAME)

    override val DB_PASSWORD: String
        get() = storage.get("database.password" defaultsTo super.DB_PASSWORD)


    override val ERROR_REPORTING_SENTRY_ENABLED: Boolean
        get() = storage.get("error_reporting.sentry.enabled" defaultsTo super.ERROR_REPORTING_SENTRY_ENABLED)

    override val ERROR_REPORTING_SENTRY_DSN: String
        get() = storage.get("error_reporting.sentry.dsn" defaultsTo super.ERROR_REPORTING_SENTRY_DSN)


    override val TIME_TIMEZONE: String
        get() = storage.get("time.timezone" defaultsTo super.TIME_TIMEZONE)

    override val TIME_LOCALE: String
        get() = storage.get("time.locale" defaultsTo super.TIME_LOCALE)


    override val WARPS_PER_PAGE: Int
        get() = storage.get("warps.warps_per_page" defaultsTo super.WARPS_PER_PAGE)


    override val INTEGRATION_DYNMAP_WARP_ICON: String
        get() = storage.get("integrations.dynmap.warp_icon" defaultsTo super.INTEGRATION_DYNMAP_WARP_ICON)


    override val GROUPS_BUILD_PRIORITY: List<String>
        get() = storage.get("groups.build_priority" defaultsTo super.GROUPS_BUILD_PRIORITY)

    override val GROUPS_TRUST_PRIORITY: List<String>
        get() = storage.get("groups.trust_priority" defaultsTo super.GROUPS_TRUST_PRIORITY)

    override val GROUPS_DONOR_PRIORITY: List<String>
        get() = storage.get("groups.donor_priority" defaultsTo super.GROUPS_DONOR_PRIORITY)
}