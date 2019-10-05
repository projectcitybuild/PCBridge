# PCBridge

[![CircleCI](https://circleci.com/gh/andyksaw/PCBridge/tree/master.svg?style=svg)](https://circleci.com/gh/andyksaw/PCBridge/tree/master)

A Spigot/Paper plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft servers

## Project Structure
PCBridge is designed to be modular - allowing it to be simultaneously built for Spigot, Bungee, or whatever is the latest server software.

* `core`: Contains interfaces and extensions used across all modules. This is the main framework module
* `api`: Contains API requests (Retrofit)
* `entities`: The model/data layer - contains nothing but POJOs (Plain Old Java Objects) 
* `actions`: Small, reusable pieces of logic. For example, getting the ban status of a UUID
* `spigot`: Contains logic specific to just Spigot - commands, listeners, extensions, etc
* `bungee`: Contains logic specific to just Bungee (not created yet)

#### Environment
Each platform has its own Environment and Module. Spigot contains a `SpigotEnvironment` which serves as a dependency injection layer, providing Spigot specific implementations of services such as the logger, config file, chat colors, plugin hooks, etc.

#### Data Flow
Each platform has a `CommandDelegatable` and `ListenerDelegatable` which serve as the wiring between a user and their events. 

A command (represented by `Commandable`) is an event where the user types in a command such as `/ban`. A command event is received by the `CommandDelegatable` and dispatches to the relevant command handler - in this case the `BanCommand` - which performs all the required logic.

## Commands:
* `/ban <name> [reason]` - Bans a player from the PCB game network
* `/unban <name>` - Unbans a player
* `/status <name>` - Checks the ban status of a player
* `/mute <name>` - Prevents a player from sending chat messages
* `/unmute <name>` - Removes a player's mute
* `/maintenance [on|off]` - Puts the server into Maintenance Mode; no non-staff player can join while active

## Using on Server
PCBridge relies on [LuckPerms](https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) for managing ranks.
Make sure that you have the LuckPerms.jar in your server's plugin folder.