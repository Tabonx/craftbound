package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.craftbound.progression.InputSlot;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.fml.ModList;

// The integrations with other mods that are installed. This is the only place that asks which mods
// are loaded, and the only shared file that names an integration, so a mod that is absent never has
// its classes resolved. Every call into an integration goes through its guard.
public final class Integrations
{
    private record Loaded(IntegrationGuard guard, BookIntegration integration) {}

    private static final List<Loaded> LOADED = new ArrayList<>();

    public static void load()
    {
        //? if create {
        if (ModList.get().isLoaded("create"))
            add("Create", com.craftbound.client.create.CreateIntegration::new);
        //?}
    }

    private static void add(String name, Function<IntegrationGuard, BookIntegration> factory)
    {
        IntegrationGuard guard = new IntegrationGuard(name);
        guard.call(() -> Optional.of(factory.apply(guard)), Optional::<BookIntegration>empty)
                .ifPresent(integration -> LOADED.add(new Loaded(guard, integration)));
    }

    public static List<InputSlot> hiddenRequirements(Object recipe)
    {
        List<InputSlot> slots = new ArrayList<>();
        for (Loaded loaded : LOADED)
            slots.addAll(loaded.guard().call(() -> loaded.integration().hiddenRequirements(recipe), List::of));
        return slots;
    }

    public static Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        for (Loaded loaded : LOADED)
        {
            Optional<MenuBinding> binding = loaded.guard()
                    .call(() -> loaded.integration().bind(menu, transfer), Optional::empty);
            if (binding.isPresent())
                return Optional.of(binding.get().guardedBy(loaded.guard(), transfer));
        }
        return Optional.empty();
    }

    private Integrations() {}
}
