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

PCBridge assumes all Multiverse plugins are disabled and will not run alongside them; it does not check whether they are enabled. Inventory transitions are always active, and PCBridge creates a backup before its first inventory write. It keeps the existing Multiverse-Inventories folders and JSON fields. A missing profile, game-mode section, or share field leaves the player's current share unchanged. A malformed or unreadable profile is reported and is not treated as an empty profile.

Inventory profiles use the existing `groups/<group>/<player>.json`, `worlds/<world>/<player>.json`, and `players/<uuid>.json` locations. ItemStacks can be read from Bukkit serialized maps, Paper item-schema maps, or Base64 serialized bytes. Writes use Bukkit's serialized map format unless `use-byte-serialization-for-inventory-data` is enabled. Updates preserve unknown profile keys, sections, and inventory slots; they do not migrate profile files. Each profile replacement is atomic when the filesystem supports it and follows a one-time backup. A transition is not transactional across profiles: if a required source write fails, PCBridge reports the failure and does not apply destination data or advance `lastWorld`; successful independent source writes remain available for retry. Automated fixtures cover these formats, but should not be treated as a substitute for validating a server's installed Multiverse-Inventories version and representative data.

If multiple groups for one world claim the same share, PCBridge skips that share for the transition and logs the conflicting group names. Other shares continue to use their resolved group or world profiles. The Economy share is unsupported: PCBridge logs a warning and skips Economy without disabling other configured shares.

World lifecycle operations preserve unknown fields in `plugins/Multiverse-Core/worlds.yml`. Startup reports invalid entries and continues with valid worlds. A registered world is not recreated when its folder is missing or it has neither `level.dat` nor `level.dat_old`. World creation applies the configured environment, type, seed, generator, generator settings, structure generation, PvP, difficulty, and spawn; coordinate scale and respawn-world values are preserved in the registry but are not applied by this lifecycle service. Regenerate and delete require confirmation and keep a backup under `plugins/PCBridge/world-backups`; regeneration saves and backs up the world before unloading it, and worlds with players cannot be unloaded or regenerated. Cloning excludes `uid.dat` and `session.lock`; a failed clone removes its partial target when Paper allows the target to unload.

Portal travel is cancelled and logged when an explicit or automatic destination is missing or unavailable. Nether routing directly assigns the scaled destination coordinates after checking the target world border; it does not perform vanilla portal search, reuse, or creation. PCBridge therefore does not promise vanilla-equivalent portal behavior. End platforms are placed only in a safe area within 16 blocks of the spawn and in already loaded chunks; existing occupied blocks are not replaced, and travel is cancelled if no such area exists. This safety behavior applies even if Multiverse-NetherPortals has `bounceback: false`.

Potion effects accept the current Bukkit serialized format and the legacy JSON-string format. When saving a legacy string field, PCBridge keeps that field format and preserves unknown entries.

Custom portals read and update `plugins/Multiverse-Portals/portals.yml` in place. Use `/mvp wand` to select two corners, then `/mvp create <name> [destination]`. Destinations support `w:<world>[:<direction>]`, `e:<world>:<x,y,z>`, and `p:<portal>`; `/mvp destination <name> <destination>` updates an existing portal. Other action types and portals with entry fees are reported as unsupported and are not activated.

Do not remove the Multiverse plugins until the installed versions and configuration have been tested on a server copy.

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