# PCBridge
A Bukkit (and Spigot) plugin to bridge [Project City Build](www.projectcitybuild.com) and its game servers

### Current features:
* Ban system
* Mojang UUID lookup
  * Username -> UUID
  * Username -> Name history

* JDBC MySQL wrapper

### Want to contribute?
Great! But first you'll need to:

1. Set up a local MySQL/MariaDB database.
2. Run the plugin once to generate the config file (plugin.yml)
3. Add your database connection details in the config file
4. Download (manually or via maven) the below jar file dependencies

### Dependencies
PCBridge depends on Apache's [DBCP2 library](https://commons.apache.org/proper/commons-dbcp/) for database connection pooling. Once the dependencies are downloaded (eg. via Maven), ensure they are in the <b>plugins/lib</b> folder.

######Required inside 'plugins/lib' folder
* commons-dbcp2-2.0.1.jar
* commons-pool2-2.2.jar
* commons-logging-1.1.3.jar

If you're using different versions or filenames to the ones listed above, ensure you update the project's CLASSPATH inside MANIFEST.MF.

### Commands:
* <b>/ban</b> [name] [reason]
* <b>/tempban</b> [name] [time] [reason]
* <b>/unban</b> [name]
* <b>/checkban</b> [name]
* <b>/uuid uuid</b> [name]
* <b>/uuid history</b> [name]
