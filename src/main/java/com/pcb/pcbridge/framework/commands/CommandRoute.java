package com.pcb.pcbridge.framework.commands;

import com.pcb.pcbridge.framework.commands.AbstractCommand;

class CommandRoute {
    private boolean isRegistered = false;
    private AbstractCommand command;

    public CommandRoute(AbstractCommand command) {
        this.command = command;
    }

    public AbstractCommand getCommand() {
        return command;
    }

    public boolean isRegistered() {
        return isRegistered;
    }
}