package com.craftbound.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.craftbound.client.jei.TransferPlacer;
import com.craftbound.progression.InputSlot;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.fml.ModList;

// The integrations with other mods that are installed. This is the only place that asks which mods
// are loaded, and the only shared file that names an integration, so a mod that is absent never has
// its classes resolved. Each integration gets a guard of its own, so one mod that no longer fits
// leaves the others running. Vanilla's recipe-book screens come first, through the same route.
public final class Integrations
{
    private static final Integrations INSTALLED = new Integrations(new VanillaIntegration());

    private final List<BookIntegration> integrations = new ArrayList<>();

    Integrations(BookIntegration... builtIn)
    {
        integrations.addAll(List.of(builtIn));
    }

    public static void load()
    {
        //? if create {
        if (ModList.get().isLoaded("create"))
            INSTALLED.add(new IntegrationGuard("Create"), com.craftbound.client.create.CreateIntegration::new);
        if (ModList.get().isLoaded("ponder"))
            INSTALLED.add(new IntegrationGuard("Ponder"), com.craftbound.client.create.PonderIntegration::new);
        //?}
    }

    public static List<InputSlot> hiddenRequirements(Object recipe)
    {
        return INSTALLED.requirements(recipe);
    }

    // A screen no integration owns takes recipes through the transfer handler a mod gave JEI.
    public static MenuBinding bind(AbstractContainerMenu menu)
    {
        return INSTALLED.binding(menu, new TransferPlacer(menu));
    }

    // Building the integration resolves the mod's classes, so a mod that moved them is caught here.
    void add(IntegrationGuard guard, Function<IntegrationGuard, BookIntegration> factory)
    {
        BookIntegration integration = guard.call(() -> factory.apply(guard), () -> null);
        if (integration != null)
            integrations.add(integration);
    }

    List<InputSlot> requirements(Object recipe)
    {
        List<InputSlot> slots = new ArrayList<>();
        for (BookIntegration integration : integrations)
            slots.addAll(integration.hiddenRequirements(recipe));
        return slots;
    }

    MenuBinding binding(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        for (BookIntegration integration : integrations)
        {
            Optional<MenuBinding> binding = integration.bind(menu, transfer);
            if (binding.isPresent())
                return binding.get();
        }
        return MenuBinding.placing(transfer);
    }
}
