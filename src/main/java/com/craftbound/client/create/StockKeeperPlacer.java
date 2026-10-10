package com.craftbound.client.create;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.RecipePlacer;
import com.craftbound.client.jei.RecipeGroup;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.crafting.RecipeHolder;

// Create's transfer handler adds a recipe to the stock keeper's order once and refuses it after
// that. Placing it again raises the amount instead, as scrolling over the ordered recipe does.
public final class StockKeeperPlacer implements RecipePlacer
{
    private final RecipePlacer transfer;
    private final StockKeeperRequestMenu menu;

    public StockKeeperPlacer(RecipePlacer transfer, AbstractContainerMenu menu)
    {
        this.transfer = transfer;
        this.menu = (StockKeeperRequestMenu) menu;
    }

    @Override
    public boolean offers(IRecipeLayoutDrawable<?> layout)
    {
        return transfer.offers(layout);
    }

    @Override
    public boolean canPlace(IRecipeLayoutDrawable<?> layout)
    {
        return ordered(layout).isPresent() || transfer.canPlace(layout);
    }

    // As much again as Create's own first request: one craft, or a stack with shift.
    @Override
    public void place(IRecipeLayoutDrawable<?> layout, boolean placeAll)
    {
        Optional<CraftableBigItemStack> ordered = ordered(layout);
        if (ordered.isEmpty())
            transfer.place(layout, placeAll);
        else if (menu.screenReference instanceof StockKeeperRequestScreen screen)
            screen.requestCraftable(ordered.get(), placeAll ? ordered.get().stack.getMaxStackSize() : 1);
    }

    @Override
    public List<Component> refusal(IRecipeLayoutDrawable<?> layout)
    {
        return transfer.refusal(layout);
    }

    @Override
    public Set<IRecipeSlotView> missing(IRecipeLayoutDrawable<?> layout)
    {
        return transfer.missing(layout);
    }

    @Override
    public void showMissing(GuiGraphics graphics, IRecipeLayoutDrawable<?> layout, int mouseX, int mouseY)
    {
        transfer.showMissing(graphics, layout, mouseX, mouseY);
    }

    @Override
    public int menuGroup(List<RecipeGroup> groups)
    {
        return transfer.menuGroup(groups);
    }

    private Optional<CraftableBigItemStack> ordered(IRecipeLayoutDrawable<?> layout)
    {
        if (!(menu.screenReference instanceof StockKeeperRequestScreen screen)
                || !(layout.getRecipe() instanceof RecipeHolder<?> holder))
            return Optional.empty();
        return screen.recipesToOrder.stream()
                .filter(order -> order.recipe == holder.value())
                .findFirst();
    }
}
