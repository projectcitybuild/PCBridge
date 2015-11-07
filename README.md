# PCBridge
A Bukkit (and Spigot) plugin to bridge [Project City Build](www.projectcitybuild.com) and its game servers

### Current features:
* Ban system
  * Supports UUID bans
  * Temp bans
  * UUID lookup on offline players
* Mojang UUID lookup
  * Username -> UUID
  * Username -> Name history
* Chat swear filter
* Rank synchronisation with forums (SMF)
  
* JDBC MySQL wrapper

### Want to contribute?
Great! But first you'll need to:

1. Set up a local MySQL database.
2. Run the plugin once to generate the config file (plugin.yml)
3. Add in your MySQL connection details there as necessary
4. Download (manually or via maven) the below jar file dependencies

### Dependencies
PCBridge depends on Apache's [DBCP2 library](https://commons.apache.org/proper/commons-dbcp/) for database connection pooling. Once the dependencies are downloaded (eg. via Maven), ensure they are in the <b>plugins/lib</b> folder.

PCBridge also depends on [Vault](http://dev.bukkit.org/bukkit-plugins/vault/) for hooking into the permissions API. Ensure this is in the <b>plugins</b> (not lib) folder.

######Required inside 'plugins' folder
* Vault.jar

######Required inside 'plugins/lib' folder
* commons-dbcp2-2.0.1.jar
* commons-pool2-2.2.jar
* commons-logging-1.1.3.jar

If you're using different versions or filenames to the ones listed above, ensure you update the project's CLASSPATH inside MANIFEST.MF.

### Commands:
* <b>/ban</b> [name] [reason]
* <b>/tempban</b> [name] [time] [reason]
* <b>/unban</b> [name]
* <b>/lookup</b> [name]
* <b>/uuid</b> [name]
* <b>/uuid history</b> [name]
* <b>/swearblock</b> [on/off]   --on/off argument optional
* <b>/pcbridge config</b> [key] [value]
* <b>/pcbridge reload</b>
