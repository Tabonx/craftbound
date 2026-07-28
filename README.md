# Craftbound

A [Create](https://modrinth.com/mod/create)-focused Minecraft mod that reworks the
recipe book: a restyled UI, extra options, and a custom recipe-unlock/progression
system that surfaces Create's machine recipes.

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge 21.1.176
- **Requires:** Create 6.x

## Development

Prerequisites: JDK 21 and IntelliJ IDEA.

1. Open the project folder in IntelliJ (it imports the Gradle build automatically).
   Set the Project SDK to Java 21 if prompted.
2. Run the **client** run configuration to launch a dev Minecraft instance with
   Craftbound (and Create) loaded.
3. Build a distributable jar with:

   ```sh
   ./gradlew build
   ```

   The jar is written to `build/libs/`.

Useful Gradle commands:

- `./gradlew --refresh-dependencies` — refresh the local dependency cache.
- `./gradlew clean` — clear build outputs (does not touch your source).

## License

Craftbound is licensed under the [MIT License](LICENSE).

## Resources

- Create developer docs: https://wiki.createmod.net/developers/
- NeoForge documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
