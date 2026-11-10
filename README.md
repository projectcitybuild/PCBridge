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

### Multiverse transition

PCBridge reads Multiverse data from the existing plugin folders. It does not move Multiverse-Inventories profiles into the PCBridge data folder or change their JSON format.

Start by reviewing the existing files with `/inventories inspect`, `/inventories groups`, `/portals list`, and `/worlds manage list`. Keep an independent backup of the server before changing plugin ownership. PCBridge creates a backup before its first active inventory write, but that backup is not a substitute for a server backup.

PCBridge assumes all Multiverse plugins are disabled and will not run alongside them; it does not check whether they are enabled. Inventory transitions are always active, and PCBridge creates a backup before its first inventory write. Legacy portal links in `Multiverse-NetherPortals/config.yml` are read without changing that file.

Custom portals read and update `plugins/Multiverse-Portals/portals.yml` in place. Use `/mvp wand` to select two corners, then `/mvp create <name> [destination]`. Destinations support `w:<world>[:<direction>]`, `e:<world>:<x,y,z>`, and `p:<portal>`; `/mvp destination <name> <destination>` updates an existing portal. Other action types and portals with entry fees are reported as unsupported and are not activated.

Do not remove the Multiverse plugins until the installed versions and configuration have been tested on a server copy. The economy inventory share needs a Vault-compatible economy provider and is not supported by PCBridge yet; PCBridge reports an error rather than silently dropping that share.

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

To boot up the development Minecraft server

```
./gradlew runServer
```

Then connect to `localhost` in Minecraft

### End-to-end tests

Install the Node.js version configured in `.mise.toml` with mise, then run the Plugwright
tests. Plugwright starts an isolated Paper server and connects real, offline player bots.

```
mise install
mise exec -- ./gradlew :app:paper-plugin:plugwrightTest
```

The server workspace, generated tests, and Node dependencies live under
`app/paper-plugin/src/test/e2e`.