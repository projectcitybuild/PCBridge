package com.projectcitybuild.features.mail.repositories

import com.projectcitybuild.modules.database.DataSource
import javax.inject.Inject

class MailRepository @Inject constructor(
    private val dataSource: DataSource,
) {

    fun readAll() {

    }
}