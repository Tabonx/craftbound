# Craftbound

A [Create](https://modrinth.com/mod/create)-focused Minecraft mod that reworks the
recipe book: a restyled UI, extra options, and a custom recipe-unlock/progression
system that surfaces Create's machine recipes.

- **Minecraft:** 1.21.1
- **Mod loader:** NeoForge 21.1.244
- **Requires:** Create 6.x
- **Incompatible with:** JEI — Craftbound embeds the recipe infrastructure it needs
  and cannot run alongside the full mod.

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

## Releasing

Push a `v`-prefixed tag; the release workflow builds the jar and publishes a GitHub
release from it. The tag is the version of record — `mod_version` in
`gradle.properties` is only used by local and untagged builds.

```sh
git tag v0.1.0 && git push origin v0.1.0
```

## License

Craftbound is licensed under the [MIT License](LICENSE). It includes source code from
Just Enough Items, also MIT licensed — see [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Resources

- Create developer docs: https://wiki.createmod.net/developers/
- NeoForge documentation: https://docs.neoforged.net/
- NeoForged Discord: https://discord.neoforged.net/
