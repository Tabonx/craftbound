package com.craftbound.client.book;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.craftbound.Craftbound;
import com.craftbound.client.compat.Canvas;
import com.craftbound.client.compat.Input;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookRecipes;
import com.craftbound.client.jei.RecipeGroup;
import com.craftbound.client.placement.RecipePlacer;
import com.craftbound.client.progression.Progression;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// The place button, drawn where the craftable filter sits while browsing. It shows only for recipes
// the open screen can take, and greys out while placing would not work. What it shows is asked of
// the placer once a tick and whenever the shown recipe changes, never more than that.
final class PlaceControl
{
    private static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe"),
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe_disabled"),
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/place_recipe_highlighted"));
    private static final Tooltip TOOLTIP = Tooltip.create(
            Component.translatable("craftbound.recipebook.place")
                    .append(CommonComponents.NEW_LINE)
                    .append(Component.translatable("craftbound.recipebook.place.all")
                            .withStyle(ChatFormatting.GRAY)));

    private final ImageButton button;
    private Rect area = new Rect(0, 0, 0, 0);
    private RecipePlacer placer = null;
    private IRecipeLayoutDrawable<?> shown = null;
    private boolean stale = true;
    private List<Component> refusal = List.of();
    // The recipe whose greyed button was clicked, to mark what it is missing.
    private IRecipeLayoutDrawable<?> marked = null;

    PlaceControl()
    {
        button = new ImageButton(0, 0, BookGeometry.BUTTON_W, BookGeometry.BUTTON_H, SPRITES,
                b -> placer.place(shown, Input.shiftDown()));
        button.setTooltip(TOOLTIP);
        button.visible = false;
    }

    void bind(RecipePlacer placer)
    {
        this.placer = placer;
        stale = true;
    }

    void layout(BookGeometry geometry)
    {
        area = geometry.place();
        button.setPosition(area.x(), area.y());
    }

    void tick()
    {
        stale = true;
    }

    // The recipe on show, or null while browsing.
    void show(IRecipeLayoutDrawable<?> recipe)
    {
        if (recipe == shown && !stale)
            return;
        shown = recipe;
        stale = false;
        button.visible = recipe != null && placer != null && placer.offers(recipe);
        if (!button.visible)
            return;
        button.active = placer.canPlace(recipe);
        List<Component> why = button.active ? List.of() : placer.refusal(recipe);
        if (!why.equals(refusal))
        {
            refusal = why;
            button.setTooltip(why.isEmpty() ? TOOLTIP : Tooltip.create(CommonComponents.joinLines(why)));
        }
    }

    boolean shown()
    {
        return button.visible;
    }

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        if (button.visible)
            Canvas.widget(button, graphics, mouseX, mouseY, partialTick);
    }

    boolean click(double mouseX, double mouseY, int mouseButton)
    {
        if (!button.visible)
            return false;
        if (Input.click(button, mouseX, mouseY, mouseButton))
            return true;
        // An inactive button takes no clicks, so the greyed one is hit-tested here.
        if (button.active || !area.contains(mouseX, mouseY))
            return false;
        BookSounds.click();
        marked = shown;
        return true;
    }

    boolean marks(IRecipeLayoutDrawable<?> recipe)
    {
        return recipe == marked && recipe == shown && button.visible;
    }

    Set<IRecipeSlotView> missing(IRecipeLayoutDrawable<?> recipe)
    {
        return marks(recipe) ? placer.missing(recipe) : Set.of();
    }

    void showMissing(GuiGraphics graphics, IRecipeLayoutDrawable<?> recipe, int mouseX, int mouseY)
    {
        if (marks(recipe))
            placer.showMissing(graphics, recipe, mouseX, mouseY);
    }

    int menuGroup(List<RecipeGroup> groups)
    {
        return placer == null ? 0 : placer.menuGroup(groups);
    }

    // Places as many sets as the inventory allows, as shift on the button does.
    boolean quickPlace(BookIngredient ingredient)
    {
        if (placer == null)
            return false;
        Optional<IRecipeLayoutDrawable<?>> recipe = placer.firstPlaceable(
                BookRecipes.groupsFor(ingredient, RecipeIngredientRole.OUTPUT, Progression.gate()));
        recipe.ifPresent(found ->
        {
            BookSounds.click();
            placer.place(found, true);
        });
        return recipe.isPresent();
    }
}
