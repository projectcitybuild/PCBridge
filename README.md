# PCBridge

![CI](https://github.com/projectcitybuild/PCBridge/workflows/CI/badge.svg?branch=master)

A BungeeCord and Spigot plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft servers

## Requirements
* MySQL/MariaDB server
* [LuckPerms](https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) installed on all servers

## Installation
* Place the JAR file inside the `plugins` folder of both the Bungeecord proxy server, and all node Spigot servers.
* Run the server at least once to generate the `config.yml` file on each server
* Update the generated config file with the required values (API token and MySQL connection details)
