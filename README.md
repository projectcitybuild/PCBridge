# PCBridge

![CI](https://github.com/projectcitybuild/PCBridge/workflows/CI/badge.svg?branch=master)

A BungeeCord + Spigot/Paper plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft servers

## Project Structure
PCBridge is designed to be modular - allowing it to be simultaneously built for Spigot, Bungee, or whatever is the latest server software.

* `core`: Contains interfaces and extensions used across all modules. This is the main framework module
    * `network`: Contains API requests (Retrofit) and API clients (PCB, Mojang)
    * `entities`: The model/data layer. Contains nothing but PODOs (Plain Old Data Objects) 
* `modules`: Contains domain-specific logic such as player banning and rank management.
* `platforms`: Contains "controller" logic specific to each environment (eg. Spigot). Consists mainly of Commands and Listeners that simply call `modules` code and output the result to the user.

#### Environment
Each platform has its own `Environment` implementation that serves as a dependency injection layer. Spigot has `SpigotEnvironment`, providing Spigot specific implementations of services such as the logger, config file, chat colors, plugin hooks, etc.

#### Commands & Listeners
Each platform has a `CommandDelegatable` and `ListenerDelegatable` which serve as the wiring between a user and their events. 

A command (represented by `Commandable`) is an event where the user types in a command such as `/ban`. A command event is received by the `CommandDelegatable` and dispatches to the relevant command handler - in this case the `BanCommand` - which performs all the required logic.

## Commands:
* `/ban <name> [reason]` - Bans a player from the PCB game network
* `/unban <name>` - Unbans a player
* `/checkban <name>` - Checks the ban status of a player
* `/mute <name>` - Prevents a player from sending chat messages
* `/unmute <name>` - Removes a player's mute

## Using on Server
PCBridge relies on [LuckPerms](https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) for managing ranks.
Make sure that you have the LuckPerms.jar in your server's plugin folder.