package com.craftbound.progression;

import java.util.List;
import java.util.Set;

import net.neoforged.neoforge.common.ModConfigSpec;

// Server config, so a pack decides how progression behaves and every client on the server agrees:
// NeoForge syncs SERVER configs to clients, and the book filters client-side against them. On a
// server without Craftbound nothing is synced, so the client's own copy decides.
public final class ProgressionConfig
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Hide recipes until the player has obtained what they are made from.")
            .define("enabled", true);

    private static final ModConfigSpec.EnumValue<UnlockRule> RULE = BUILDER
            .comment("ALL_INPUTS: every ingredient must have been obtained (a slot with alternatives",
                    "needs just one of them). ANY_INPUT: one obtained ingredient is enough.")
            .defineEnum("rule", UnlockRule.ALL_INPUTS);

    private static final ModConfigSpec.BooleanValue GATE_CATEGORIES = BUILDER
            .comment("Hide a whole recipe category until its workstation has been obtained,",
                    "e.g. no mixing recipes before a Mechanical Mixer.")
            .define("gateCategories", true);

    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXEMPT_CATEGORIES = BUILDER
            .comment("Recipe categories never gated on their workstation. Dropping minecraft:crafting",
                    "from this list strands a new player, who needs the crafting category to find out",
                    "how a crafting table is made.")
            .defineList("exemptCategories", List.of("minecraft:crafting"),
                    () -> "", element -> element instanceof String);

    private static final ModConfigSpec.BooleanValue GATE_HINTS = BUILDER
            .comment("Mark the items that would unlock more recipes only once the player has bound a",
                    "Bookbinder's Lens into their book. Never applies on a server without Craftbound,",
                    "where the lens cannot be obtained and the marks always show.")
            .define("gateHintsBehindLens", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    // Falls back to the defaults until the config is loaded, so the book never has to special-case
    // the window between joining and the server's config arriving.
    private static final ProgressionRules DEFAULTS =
            new ProgressionRules(true, UnlockRule.ALL_INPUTS, true, Set.of("minecraft:crafting"));

    public static ProgressionRules rules()
    {
        if (!SPEC.isLoaded())
            return DEFAULTS;
        return new ProgressionRules(ENABLED.get(), RULE.get(), GATE_CATEGORIES.get(),
                Set.copyOf(EXEMPT_CATEGORIES.get()));
    }

    public static boolean gateHintsBehindLens()
    {
        return !SPEC.isLoaded() || GATE_HINTS.get();
    }

    private ProgressionConfig() {}
}
