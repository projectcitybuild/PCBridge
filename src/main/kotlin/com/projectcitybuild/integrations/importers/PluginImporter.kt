package com.projectcitybuild.integrations.importers

interface PluginImporter {

    /**
     * Runs a process to import data from a 3rd-party plugin.
     * Importers are not guaranteed to be asynchronous or thread-safe.
     *
     * @return Whether the import was a success
     */
    fun run(): Boolean
}