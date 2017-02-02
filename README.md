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
3. Compile PCBridge into a .jar and place it in the <b>plugins/</b> folder
4. Run the server once to generate the config file (config.yml)
5. Add your database connection details into the config file

Be sure to check the [Wiki](https://github.com/andimage/PCBridge/wiki/Contributing) on how PCBridge works.

### Dependencies
PCBridge uses Maven to shade dependencies into the final JAR file.

### References / Resources
* http://wiki.bukkit.org/Plugin_Tutorial - General Bukkit plugin dev tutorial
* https://github.com/aikar/TaskChain - TaskChain - Asynchronous task chaining
* https://github.com/brettwooldridge/HikariCP - HikariCP - Connection pool


### Commands:
* <b>/ban</b> <name> [reason]
* <b>/tempban</b> <name> <time> [reason]
* <b>/unban</b> <name>
* <b>/checkban</b> <name>
* <b>/showbans</b> <name>
* <b>/uuid</b> <name>
* <b>/uuid history</b> <name>
* <b>/buyskull player:<name> [quantity]
* <b>/mute</b> <name>
* <b>/unmute</b> <name>
* <b>/swearblock</b> <on|off>
* <b>/tplastpos</b> <name>
* <b>/rescue</b> <name> [x] [y] [z] [world]
* <b>/prefix</b> <name> <prefix>
* <b>/suffix</b> <name> <suffix>
* <b>/pcbridge</b> maintenance <on|off>
* <b>/pcbridge</b> migrate <migration>
* <b>/pcbridge</b> reload
* <b>/pcbridge</b> reloadconfig