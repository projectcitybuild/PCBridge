# PCBridge
A Bukkit (and Spigot) plugin to bridge [Project City Build](www.projectcitybuild.com) and its game servers

### Current features:
* Ban system
  * Cached look ups
  * Database queries queued and executed async
* Mojang UUID lookup
  * Username -> UUID
  * Username -> Name history
* Command to purchase player heads
* Maintenance mode (puts server into permission-based whitelist mode)

* JDBC MySQL wrapper

### Want to contribute?
Great! But first you'll need to:

1. Set up a local MySQL/MariaDB database.
2. Have the server software (Bukkit/Spigot) downloaded
3. Compile the plugin into a .jar file using your choice of IDE 
4. Put the compiled .jar into the <b>plugins</b> folder
5. Run the server once to generate the config file (config.yml)
6. Add your database connection details into the config file
7. Download (manually or via Maven) the below jar file dependencies

### Dependencies
PCBridge depends on Apache's [DBCP2 library](https://commons.apache.org/proper/commons-dbcp/) for database connection pooling. Once the dependencies are downloaded (eg. via Maven), ensure they are in the <b>plugins/lib</b> folder.

######Required inside 'plugins/lib' folder
* commons-dbcp2-2.0.1.jar
* commons-pool2-2.2.jar
* commons-logging-1.1.3.jar

#####Required inside 'plugins/' folder
* Vault.jar
* Any economy plugin (iConomy, Essentials, etc)

If you're using different versions or filenames to the ones listed above, ensure you update the project's CLASSPATH inside MANIFEST.MF.

###Pre set-up server
You can download a pre set-up Spigot (1.9.4) server here: https://dl.dropboxusercontent.com/u/39619072/Server.rar.
This contains all the dependencies set up and configuration files pre-generated.

### References / Resources
* http://wiki.bukkit.org/Plugin_Tutorial

### Commands:
* <b>/ban</b> [name] [reason]
* <b>/tempban</b> [name] [time] [reason]
* <b>/unban</b> [name]
* <b>/checkban</b> [name]
* <b>/uuid uuid</b> [name]
* <b>/uuid history</b> [name]
* <b>/buyhead player:[name] [quantity]
