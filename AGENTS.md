Craftbound is a NeoForge 1.21.1 mod for the Create ecosystem that reworks Minecraft's vanilla recipe book.
The goal is a restyled recipe-book UI with extra options, a custom recipe-unlock/progression system, and support for showing Create's machine recipe categories.

Use fewer comments. Only add a comment when the code would be genuinely unclear without it. Prefer clearer code over comments, and omit comments when in doubt.

## Code Style
- Always strive for concise, simple and elegant solutions
- If a problem can be solved in a simpler way, propose it.

## Testing
- Write code to be testable where possible: keep pure logic separate from Minecraft/engine code so it can be unit-tested without launching the game.
- Prefer extracting core logic into plain methods/classes and keeping engine-touching code (Mixins, event handlers, rendering) as thin shells over it.
- Add tests where they carry signal (logic, serialization/persistence); don't try to test rendering.

