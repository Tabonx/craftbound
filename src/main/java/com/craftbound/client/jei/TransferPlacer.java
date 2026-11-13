package com.craftbound.client.jei;

import java.util.List;

import com.craftbound.client.placement.RecipePlacer;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;

// Places through the transfer handler a mod gave JEI for its screen, as JEI's own "+" button does.
public final class TransferPlacer implements RecipePlacer
{
    private static final IRecipeTransferError NO_HANDLER = () -> IRecipeTransferError.Type.INTERNAL;

    private final AbstractContainerMenu menu;

    // A handler may count a whole storage network to answer, so the answer is kept for the tick.
    private IRecipeLayoutDrawable<?> checked;
    private int checkedTick;
    private IRecipeTransferError checkedError;

    public TransferPlacer(AbstractContainerMenu menu)
    {
        this.menu = menu;
    }

    // JEI hides its button for an internal error and greys it for the others.
    @Override
    public boolean offers(IRecipeLayoutDrawable<?> layout)
    {
        IRecipeTransferError error = check(layout);
        return error == null || error.getType() != IRecipeTransferError.Type.INTERNAL;
    }

    @Override
    public boolean canPlace(IRecipeLayoutDrawable<?> layout)
    {
        IRecipeTransferError error = check(layout);
        return error == null || error.getType().allowsTransfer;
    }

    @Override
    public void place(IRecipeLayoutDrawable<?> layout, boolean placeAll)
    {
        transfer(layout, placeAll, true);
        checked = null;
    }

    @Override
    public List<Component> refusal(IRecipeLayoutDrawable<?> layout)
    {
        IRecipeTransferError error = check(layout);
        return error == null ? List.of() : TransferErrors.tooltip(error);
    }

    @Override
    public void showMissing(GuiGraphics graphics, IRecipeLayoutDrawable<?> layout, int mouseX, int mouseY)
    {
        IRecipeTransferError error = check(layout);
        if (error != null)
            error.showError(graphics, mouseX, mouseY, layout.getRecipeSlotsView(), 0, 0);
    }

    @Override
    public int menuGroup(List<RecipeGroup> groups)
    {
        for (int i = 0; i < groups.size(); i++)
            if (offers(groups.get(i).recipes().get(0)))
                return i;
        return 0;
    }

    private IRecipeTransferError check(IRecipeLayoutDrawable<?> layout)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        int tick = player == null ? 0 : player.tickCount;
        if (layout != checked || tick != checkedTick)
        {
            checkedError = transfer(layout, false, false);
            checked = layout;
            checkedTick = tick;
        }
        return checkedError;
    }

    private <R> IRecipeTransferError transfer(IRecipeLayoutDrawable<R> layout, boolean placeAll, boolean doTransfer)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        var handler = TransferHandlers.handlerFor(menu, layout.getRecipeCategory());
        if (player == null || handler.isEmpty())
            return NO_HANDLER;
        // Null is JEI's "it worked", so the answer cannot go through an Optional.
        return handler.get().transferRecipe(menu, layout.getRecipe(), layout.getRecipeSlotsView(),
                player, placeAll, doTransfer);
    }
}
