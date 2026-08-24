Craftbound is a NeoForge 1.21.1 mod for the Create ecosystem that reworks Minecraft's vanilla recipe book.

The Mod should work with any other minecraft mod just as JEI, it should be able to load plugins and be generic to not miss anything that JEI would include.

The goal is a restyled recipe-book UI with extra options, a custom recipe-unlock/progression system, and support for showing Create's machine recipe categories.

The goal for the UI is to have it look as much as Minecraft Vanilla. Try to match the Mojang's design language. 

Use fewer comments. Only add a comment when the code would be genuinely unclear without it. Prefer clearer code over comments, and omit comments when in doubt.

## Code Style
- Always strive for concise, simple and elegant solutions
- If a problem can be solved in a simpler way, propose it.
- Strive to create code using SOLID and DRY principles

## Testing
- Write code to be testable where possible: keep pure logic separate from Minecraft/engine code so it can be unit-tested without launching the game.
- Prefer extracting core logic into plain methods/classes and keeping engine-touching code (Mixins, event handlers, rendering) as thin shells over it.
- Add tests where they carry signal (logic, serialization/persistence); don't try to test rendering.

## Project layout
- Organize code by feature/domain, not by technical layer.
- Keep client-only code (rendering, client Mixins) under a `client` package, separate from common code that also runs on the dedicated server.
- Prefer vanilla and JEI APIs over NeoForge ones. Every file that imports `net.neoforged` is a file another loader would have to reimplement, so keep those few files thin and keep the logic they wrap loader-free.

## Multiple Minecraft versions
Stonecutter builds one source tree for every supported version. `versions/<version>/gradle.properties` holds what differs; `./gradlew buildAll` builds them all.

Every version difference has one home. Take the first that fits:
1. A rename → a replacement in `build.gradle`, never a conditional. Source is written in the oldest supported version's names.
2. A changed call shape, same intent → a facade in `client/`, never the call site.
3. A file that is version-specific throughout → a copy in `src/main/java-since-<version>/`, same name and package.
4. Anything left → a `//? if >=<version> {` conditional, in the smallest block that covers it.

A file outside `client/` growing a third conditional wants a facade. A class that is mostly shared keeps a `...Base` in `src/main/java/` and lets each version subclass it under the name callers already use.

- A build takes every `-since-` directory up to its own version, newest last, so the newest copy of each file wins. `resources-since-<version>/` works the same way.
- Boundaries are per file: a directory holds only the files that changed at that version, and opening one is just creating it.
- These directories are preprocessed too, so they are still written in 1.21.1's names and may still hold a conditional for a later boundary.
- Never let a conditional branch hold only a comment. Switching versions rewrites the block in place and the branch comes back as bare text that will not compile.

Adding a version:
1. Add it to `versions(...)` in `settings.gradle`, and copy the nearest `versions/<version>/gradle.properties`, updating every dependency it names.
2. `./gradlew "Set active project to <version>"` and compile, resolving errors in the order above.
3. Add it to `publish_minecraft_versions` for the jar that should claim it.
4. `./gradlew buildAll`, then `Reset active project` before committing.
5. Launch the client once per set of versions sharing the same code. `buildAll` catches compile drift, not a mixin whose injection point stopped matching.

Build and publish against released loader versions. Where a dependency lags behind one, add a `_jei_runtime` style property and reach for it with a Gradle flag when running the game, rather than pinning what ships.

## Changelog
- Record player-facing changes in `CHANGELOG.md` under an `## <version>` heading as part of the change itself, not at release time.
- Newest version first. Add the entry to the topmost unreleased version, or start a new `## <version>` section if the top one is already released.
- Write for players, not for the diff: what changed in the game, no file or class names.
- Releases publish the section matching the tag verbatim, so nothing outside it (including the `# Changelog` heading) reaches the release notes.
- Purely internal work (build, CI, refactors, tests) does not belong there.

