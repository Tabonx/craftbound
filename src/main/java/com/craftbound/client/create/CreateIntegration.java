package com.craftbound.client.create;

import java.util.List;
import java.util.Optional;

import com.craftbound.client.integration.BookIntegration;
import com.craftbound.client.integration.IntegrationGuard;
import com.craftbound.client.integration.MenuBinding;
import com.craftbound.client.placement.RecipePlacer;
import com.craftbound.progression.InputSlot;
import com.craftbound.progression.create.HeatRequirement;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

// Everything the book knows about Create beyond what Create tells JEI: the heat a basin recipe needs,
// and the stock keeper's order and stock.
public final class CreateIntegration implements BookIntegration
{
    private final IntegrationGuard guard;

    public CreateIntegration(IntegrationGuard guard)
    {
        this.guard = guard;
    }

    // A basin's heat source is painted by the category rather than declared as a slot.
    @Override
    public List<InputSlot> hiddenRequirements(Object recipe)
    {
        return HeatRequirement.slotsFor(guard.call(() -> heatOf(recipe), () -> HeatRequirement.NONE));
    }

    private static String heatOf(Object recipe)
    {
        return recipe instanceof RecipeHolder<?> holder && holder.value() instanceof ProcessingRecipe<?, ?> processing
                ? processing.getRequiredHeat().name()
                : HeatRequirement.NONE;
    }

    @Override
    public Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        StockKeeperRequestMenu keeper = guard.call(() -> menu instanceof StockKeeperRequestMenu stockKeeper
                ? stockKeeper
                : null, () -> null);
        if (keeper == null)
            return Optional.empty();

        StockKeeperStock stock = new StockKeeperStock(guard, keeper);
        return Optional.of(new MenuBinding(new StockKeeperPlacer(guard, transfer, keeper), stock::craftable,
                stock::version));
    }
}
