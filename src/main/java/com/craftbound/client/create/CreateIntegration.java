package com.craftbound.client.create;

import java.util.List;
import java.util.Optional;

import com.craftbound.client.BookIntegration;
import com.craftbound.client.IntegrationGuard;
import com.craftbound.client.MenuBinding;
import com.craftbound.client.RecipePlacer;
import com.craftbound.progression.InputSlot;
import com.craftbound.progression.create.HeatRequirement;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

// Everything the book knows about Create beyond what Create tells JEI: the heat a basin recipe needs,
// the stock keeper's order and stock, and Ponder's view of what the player has found.
public final class CreateIntegration implements BookIntegration
{
    public CreateIntegration(IntegrationGuard guard)
    {
        PonderVisibility.guardedBy(guard);
        CraftboundPonderPlugin.register();
    }

    // A basin's heat source is painted by the category rather than declared as a slot.
    @Override
    public List<InputSlot> hiddenRequirements(Object recipe)
    {
        if (recipe instanceof RecipeHolder<?> holder && holder.value() instanceof ProcessingRecipe<?, ?> processing)
            return HeatRequirement.slotsFor(processing.getRequiredHeat().name());
        return List.of();
    }

    @Override
    public Optional<MenuBinding> bind(AbstractContainerMenu menu, RecipePlacer transfer)
    {
        if (!(menu instanceof StockKeeperRequestMenu keeper))
            return Optional.empty();
        return Optional.of(new MenuBinding(new StockKeeperPlacer(transfer, keeper),
                () -> StockKeeperStock.craftable(keeper), () -> StockKeeperStock.version(keeper)));
    }
}
