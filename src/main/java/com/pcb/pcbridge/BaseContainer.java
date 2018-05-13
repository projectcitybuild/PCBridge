package com.pcb.pcbridge;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.bukkit.plugin.PluginLogger;

public class BaseContainer extends AbstractModule {

    private PCBridge plugin;

    public BaseContainer(PCBridge plugin) {
        this.plugin = plugin;
    }

    /**
     * Provides dependency injection bindings
     * for the plugin entry file
     */
    @Override
    protected void configure() {
        bind(PCBridge.class).toInstance(plugin);
    }

    /**
     * Factory method for a logger.
     *
     * The standard Logger does not have our
     * plugin name suffixed to messages. Furthermore,
     * Guice provides a default binding for Logger
     * which we cannot override.
     *
     * @return
     */
    @Provides
    PluginLogger provideLogger() {
        return (PluginLogger)plugin.getLogger();
    }

}
