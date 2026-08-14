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

## Changelog
- Record player-facing changes in `CHANGELOG.md` under an `## <version>` heading as part of the change itself, not at release time.
- Newest version first. Add the entry to the topmost unreleased version, or start a new `## <version>` section if the top one is already released.
- Write for players, not for the diff: what changed in the game, no file or class names.
- Releases publish the section matching the tag verbatim, so nothing outside it (including the `# Changelog` heading) reaches the release notes.
- Purely internal work (build, CI, refactors, tests) does not belong there.

