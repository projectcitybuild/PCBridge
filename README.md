# PCBridge
A Spigot plugin to bridge [Project City Build](www.projectcitybuild.com) and its game servers

### Current features:
* Ban system
  * Supports temporary bans
  * Stores permanent records of all bans and unbans
* Rank synchronisation with SMF
  * Multiple rank support
* Mojang UUID lookup
  * Username -> UUID
  * Username -> Name history
* Maintenance mode (puts the server into a permission-based whitelist mode)
* Rescue stuck players
* Mute players
* Chat swear filter
* House purchasing
* Chat name prefixes & suffixes

* Framework features
  * Command registering without plugin.yml
  * Command prompts (eg. type /confirm to continue)
  * Database connection pooling
  * Vault integration
  * A few cache implementations
  * Database migrations

### Want to contribute?
Great! But first you'll need to:

1. Set up a local MySQL/MariaDB database.
2. Have Spigot downloaded and set up
3. Compile PCBridge into a .jar and place it in the plugins/ folder
4. Run the server once to generate the config file (config.yml)
5. Add your database connection details into the config file

Be sure to check the [Wiki](https://github.com/andimage/PCBridge/wiki/Contributing) on how PCBridge works.

### Dependencies
PCBridge uses Maven to shade dependencies into the final JAR file.

### References / Resources
* http://wiki.bukkit.org/Plugin_Tutorial - General Bukkit plugin dev tutorial
* https://github.com/aikar/TaskChain - TaskChain - Asynchronous task chaining
* https://github.com/brettwooldridge/HikariCP - HikariCP - Connection pool


## Commands:
* /ban [name] [reason]
* /tempban [name] [time] [reason]
* /unban [name]
* /checkban [name]
* /showbans [name]
* /uuid [name]
* /uuid history [name]
* /buyskull player:[name] [quantity]
* /mute [name]
* /unmute [name]
* /warn [name] new|silent [reason]
* /warn list [name]
* /swearblock [on|off]
* /tplastpos [name]
* /rescue [name] [x] [y] [z] [world]
* /prefix [name] [prefix]
* /suffix [name] [suffix]
* /pcbridge maintenance [on|off]
* /pcbridge migrate [migration]
* /pcbridge reload
* /pcbridge reloadconfig