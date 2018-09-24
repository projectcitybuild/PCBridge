# PCBridge
A Spigot plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft servers
(Currently being rewritten in Kotlin)

## Commands:
* `/ban <name> [reason]` - Bans a player from the PCB game network
* `/unban <name>` - Unbans a player
* `/status <name>` - Checks the ban status of a player
* `/mute <name>` - Prevents a player from sending chat messages
* `/unmute <name>` - Removes a player's mute
* `/maintenance [on|off]` - Puts the server into Maintenance Mode; no non-staff player can join while active
* `/prefix <name> [prefix]` - Appends a prefix to a player's display name (blank = reset)
* `/suffix <name> [suffix]` - Appends a suffix to a player's display name (blank = reset)