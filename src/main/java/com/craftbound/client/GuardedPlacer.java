package com.craftbound.client;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.client.jei.RecipeGroup;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

// An integration's placer, answered by the fallback once the integration is switched off.
final class GuardedPlacer implements RecipePlacer
{
    private final IntegrationGuard guard;
    private final RecipePlacer placer;
    private final RecipePlacer fallback;

    GuardedPlacer(IntegrationGuard guard, RecipePlacer placer, RecipePlacer fallback)
    {
        this.guard = guard;
        this.placer = placer;
        this.fallback = fallback;
    }

    @Override
    public boolean offers(IRecipeLayoutDrawable<?> layout)
    {
        return guard.call(() -> placer.offers(layout), () -> fallback.offers(layout));
    }

    @Override
    public boolean canPlace(IRecipeLayoutDrawable<?> layout)
    {
        return guard.call(() -> placer.canPlace(layout), () -> fallback.canPlace(layout));
    }

    @Override
    public void place(IRecipeLayoutDrawable<?> layout, boolean placeAll)
    {
        if (!guard.run(() -> placer.place(layout, placeAll)))
            fallback.place(layout, placeAll);
    }

    @Override
    public Set<IRecipeSlotView> missing(IRecipeLayoutDrawable<?> layout)
    {
        return guard.call(() -> placer.missing(layout), () -> fallback.missing(layout));
    }

    @Override
    public List<Component> refusal(IRecipeLayoutDrawable<?> layout)
    {
        return guard.call(() -> placer.refusal(layout), () -> fallback.refusal(layout));
    }

    @Override
    public void showMissing(GuiGraphics graphics, IRecipeLayoutDrawable<?> layout, int mouseX, int mouseY)
    {
        if (!guard.run(() -> placer.showMissing(graphics, layout, mouseX, mouseY)))
            fallback.showMissing(graphics, layout, mouseX, mouseY);
    }

    @Override
    public int menuGroup(List<RecipeGroup> groups)
    {
        return guard.call(() -> placer.menuGroup(groups), () -> fallback.menuGroup(groups));
    }

    @Override
    public Optional<IRecipeLayoutDrawable<?>> firstPlaceable(List<RecipeGroup> groups)
    {
        return guard.call(() -> placer.firstPlaceable(groups), () -> fallback.firstPlaceable(groups));
    }
}
