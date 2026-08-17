# Craftbound

Recipe book replacement to keep items hidden until you find the ingredients they need.

Craftbound replaces Minecraft's recipe book with one that adds recipes for all the mods
you have installed, through JEI, and turns them into a way to progressively discover the
game as you play.

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge 21.1.244
- **Required:** JEI 19.21.0 or newer. It is what knows every mod's recipes, so the book
  is built on it.
- **Optional:** Create 6.x. Without it the book simply has no Create recipes to show,
  and the Ponder integration stays inactive.

## Development

Prerequisites: JDK 21 and IntelliJ IDEA.

1. Open the project folder in IntelliJ (it imports the Gradle build automatically).
   Set the Project SDK to Java 21 if prompted.
2. Run the **client** run configuration to launch a dev Minecraft instance with
   Craftbound and Create loaded. Adding `-PnoCreate` starts the same client without
   Create, Ponder, Flywheel and Registrate, for testing that the mod still works
   when its optional dependency is absent.
3. Build a distributable jar with:

   ```sh
   ./gradlew build
   ```

   The jar is written to `build/libs/`.

Useful Gradle commands:

- `./gradlew runClient` launches the dev client; add `-PnoCreate` to launch it without Create.
- `./gradlew --refresh-dependencies` refreshes the local dependency cache.
- `./gradlew clean` clears build outputs (does not touch your source).

## License

Craftbound is licensed under the [MIT License](LICENSE).

## Resources

- Create developer docs: https://wiki.createmod.net/developers/
- NeoForge documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
