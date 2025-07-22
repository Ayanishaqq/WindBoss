# Wind Boss Plugin

A custom epic boss fight plugin for Minecraft 1.21+ servers, featuring a multi-phase Wind Boss with unique abilities inspired by the Warden, Wither, Elder Guardian, and Ender Dragon.

## Features
- **Summonable Boss**: Use `/windboss codes` to spawn the Wind Boss at your location.
- **Multi-Phase Fight**:
  - **Base Abilities**:
    - Sonic Boom: Deals 5 damage every 5 seconds.
    - Melee Attack: Deals 10 damage per hit.
    - Reactive Levitation: After 3 consecutive hits, players are launched 20 blocks away with Levitation.
  - **Wither Phase (75% HP)**: Explosion on phase start, Wither skull attacks, and flight.
  - **Elder Guardian Phase (50% HP)**: Applies Mining Fatigue to players within 75 blocks every 10 seconds.
  - **Ender Dragon Phase (25% HP)**: Gains Regeneration IV (removed if health exceeds 75%).
- **Custom Drops**: 5 Enchanted Golden Apples, 3 Diamond Blocks, and 1 custom Mace.
- **Mace Recipe Disabled**: The Wind Boss is the only source of the Mace.
- **Boss Bar**: Gray, visible server-wide within 250 blocks.
- **Admin Commands**:
  - `/windboss codes`: Summon the Wind Boss (permission: `windboss.summon`).
  - `/windboss reload`: Reload the plugin configuration (permission: `windboss.reload`).
  - `/windboss info`: View boss stats and phase info (permission: `windboss.info`).

## Installation
1. Download the latest `WindBoss-1.0.0.jar` from the [Releases](https://github.com/your-repo/WindBoss/releases) page.
2. Place the JAR file in your server's `plugins` folder.
3. Restart your server or use `/reload` to load the plugin.
4. Configure settings in `plugins/WindBoss/config.yml` if needed.

## Configuration
The plugin includes a `config.yml` file for customizable settings:
- Boss health, damage values, and phase thresholds.
- Drop quantities and custom item names.
- Boss bar visibility range and effect durations.

## Permissions
- `windboss.summon`: Allows summoning the Wind Boss.
- `windboss.reload`: Allows reloading the plugin configuration.
- `windboss.info`: Allows viewing Wind Boss stats and phase information.

## Building from Source
1. Clone the repository: `git clone https://github.com/your-repo/WindBoss.git`
2. Navigate to the project directory: `cd WindBoss`
3. Build the plugin: `mvn clean package`
4. Find the compiled JAR in the `target` folder.

## Requirements
- Minecraft server running Spigot/Paper 1.21 or higher.
- Java 17 or higher.

## Contributing
Contributions are welcome! Please submit a pull request or open an issue on the [GitHub repository](https://github.com/your-repo/WindBoss).

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
