<div align="center">
<img src="logo.jpg" alt="PCBridge" width="500">

![GitHub](https://img.shields.io/github/license/projectcitybuild/pcbridge)
[![Join us on Discord](https://img.shields.io/discord/161649330799902720.svg?label=&logo=discord&logoColor=ffffff&color=7389D8&labelColor=6A7EC2)](https://discord.gg/pcb)

Paper plugin to bridge [Project City Build](https://projectcitybuild.com) and its Minecraft server
</div>

<hr />

## Usage

### Prerequisites
* [LuckPerms](https://www.spigotmc.org/resources/luckperms-an-advanced-permissions-plugin.28140/) installed on the Minecraft server

### How to Install

1. Place the JAR file inside the `plugins` folder of the Spigot server
2. Run the server at least once to generate the `config.yml` file
3. Update the generated config file with the required values (eg. API token)
4. Restart the Spigot server

## Development

### Build

To generate the shaded JAR file
```
./gradlew shadowJar
```

### Testing

To boot up a local Minecraft server

```
./gradlew runServer
```

Then connect to `localhost` in Minecraft