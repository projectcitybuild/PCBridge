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
  
* JDBC MySQL wrapper
* SQL query builder (sql via method chaining)

### Want to contribute?
Great! But you'll first need to set up a local MySQL database named "PCBridge"

Run the plugin once to generate the config file (plugin.yml). Add in your MySQL connection details there as necessary.

PCBridge depends on Apache's [DBCP2 library](https://commons.apache.org/proper/commons-dbcp/) for database connection pooling. Once the dependencies are downloaded (eg. via Maven), ensure they are in the <b>lib/</b> folder inside your <b>plugins/</b> folder.

######Required inside 'lib' folder
* commons-dbcp2-2.0.1.jar
* commons-pool2-2.2.jar
* commons-logging-1.1.3.jar

If you're using different versions to the ones listed above, ensure you update the project's CLASSPATH.

### Commands:
* <b>/ban</b> [name] [reason]
* <b>/tempban</b> [name] [time] [reason]
* <b>/unban</b> [name]
* <b>/checkban</b> [name]
* <b>/lookup uuid</b> [name]
* <b>/lookup history</b> [name]
