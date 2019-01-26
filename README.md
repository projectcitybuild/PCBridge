# PCBridge
A Spigot plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft servers
(Currently being rewritten in Kotlin)

## Project Structure
PCBridge is designed to be modular - allowing it to be simultaneously built for Spigot, Bungee, or whatever is the latest server server.

* `core`: Contains interfaces and extensions used across all modules. This is the main framework module
* `api`: Contains API requests (Retrofit)
* `entities`: The model/data layer - contains nothing but POJOs (Plain Old Java Objects) 
* `actions`: Small, reusable pieces of logic. For example, getting the ban status of a UUID
* `spigot`: Contains logic specific to just Spigot - commands, listeners, extensions, etc
* `bungee`: Contains logic specific to just Bungee (not created yet)

#### Environment
Each target has its own Environment and Module. Spigot contains a `SpigotEnvironment` which serves as a dependency injection layer, providing Spigot specific implementations of services such as the logger, config file, chat colors, plugin hooks, etc.

#### Data Flow
Each target has a `CommandDelegatable` and `ListenerDelegatable` which serve as the wiring between a user and their events. 

A command (represented by `Commandable`) is an event where the user types in a command such as `/ban`. A command event is received by the `CommandDelegatable` and dispatches to the relevant command handler - in this case the `BanCommand` - which performs all the required logic and data flow management (think of it as a *Presenter* in MVP). Finally, the data is passed off to an interactor - in this case the `BanCommandInteractor` which just handles user interactions such as displaying text to the command sender, or handling clicking a 'Confirm' link (think of it as the *View* in MVP).

## Commands:
* `/ban <name> [reason]` - Bans a player from the PCB game network
* `/unban <name>` - Unbans a player
* `/status <name>` - Checks the ban status of a player
* `/mute <name>` - Prevents a player from sending chat messages
* `/unmute <name>` - Removes a player's mute
* `/maintenance [on|off]` - Puts the server into Maintenance Mode; no non-staff player can join while active
* `/prefix <name> [prefix]` - Appends a prefix to a player's display name (blank = reset)
* `/suffix <name> [suffix]` - Appends a suffix to a player's display name (blank = reset)