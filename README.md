# Craftbound

A Minecraft mod that reworks the recipe book: a restyled UI, extra options, and a
custom recipe-unlock/progression system. Recipes come from every installed mod
through an embedded JEI runtime, [Create](https://modrinth.com/mod/create)'s machine
categories included.

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge 21.1.244
- **Optional:** Create 6.x. Without it the book simply has no Create recipes to show,
  and the Ponder integration stays inactive.
- **Incompatible with:** JEI. Craftbound embeds the recipe infrastructure it needs
  and cannot run alongside the full mod.

## Development

Prerequisites: JDK 21 and IntelliJ IDEA.

1. Open the project folder in IntelliJ (it imports the Gradle build automatically).
   Set the Project SDK to Java 21 if prompted.
2. Run the **client** run configuration to launch a dev Minecraft instance with
   Craftbound and Create loaded. **clientNoCreate** is the same client without
   Create, Ponder, Flywheel and Registrate, for testing that the mod still works
   when its optional dependency is absent.
3. Build a distributable jar with:

   ```sh
   ./gradlew build
   ```

   The jar is written to `build/libs/`.

Useful Gradle commands:

- `./gradlew runClient` / `./gradlew runClientNoCreate` launch the two dev clients.
- `./gradlew --refresh-dependencies` refreshes the local dependency cache.
- `./gradlew clean` clears build outputs (does not touch your source).

## Releasing

Push a `v`-prefixed tag; the release workflow builds the jar and publishes a GitHub
release from it. The tag is the version of record. `mod_version` in
`gradle.properties` is only used by local and untagged builds.

```sh
git tag v0.1.0 && git push origin v0.1.0
```

## License

Craftbound is licensed under the [MIT License](LICENSE). It includes source code from
Just Enough Items, also MIT licensed. See [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Resources

- Create developer docs: https://wiki.createmod.net/developers/
- NeoForge documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
