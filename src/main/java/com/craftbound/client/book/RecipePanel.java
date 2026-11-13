package com.craftbound.client.book;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;

import com.craftbound.Craftbound;
import com.craftbound.client.bookmarks.BookmarkStore;
import com.craftbound.client.compat.Canvas;
import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookIngredients;
import com.craftbound.client.jei.BookRecipeRender;
import com.craftbound.client.jei.JeiCompat;
import com.craftbound.client.jei.RecipeGroup;
import com.craftbound.client.progression.Progression;

import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.inputs.RecipeSlotUnderMouse;
import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;

// The recipe state: a "‹ items" control back to the grid, the recipe drawn into the body by JEI's own
// category drawable, the categories on the rail, and a bookmark button for what the recipe makes.
final class RecipePanel implements BookPanel
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final WidgetSprites BACK_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    private static final Component BACK_LABEL = Component.translatable("craftbound.recipebook.back");
    private static final ResourceLocation BOOKMARK_OFF = sprite("bookmark");
    private static final ResourceLocation BOOKMARK_OFF_HL = sprite("bookmark_highlighted");
    private static final ResourceLocation BOOKMARK_ON = sprite("bookmark_on");
    private static final ResourceLocation BOOKMARK_ON_HL = sprite("bookmark_on_highlighted");
    private static final Component TOOLTIP_BOOKMARK = Component.translatable("craftbound.recipebook.bookmark");
    private static final Component TOOLTIP_BOOKMARKED = Component.translatable("craftbound.recipebook.bookmark.remove");

    private final RecipeNavigator<RecipeGroup, IRecipeLayoutDrawable<?>> navigator;
    private final BookCatalog catalog;
    private final PlaceControl place;
    private final BiPredicate<BookIngredient, RecipeIngredientRole> drill;
    private final Runnable close;
    private final BookRail rail = new BookRail();
    private BookGeometry geometry = BookGeometry.anchored(BookGeometry.WIDTH, 0, BookGeometry.HEIGHT, true);
    private PendingTooltip tooltip = PendingTooltip.NONE;

    // `drill` shows an ingredient's recipes in the given role, and says whether it had any.
    RecipePanel(RecipeNavigator<RecipeGroup, IRecipeLayoutDrawable<?>> navigator, BookCatalog catalog,
            PlaceControl place, BiPredicate<BookIngredient, RecipeIngredientRole> drill, Runnable close)
    {
        this.navigator = navigator;
        this.catalog = catalog;
        this.place = place;
        this.drill = drill;
        this.close = close;
        syncRail();
    }

    private static ResourceLocation sprite(String name)
    {
        return ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/" + name);
    }

    void syncRail()
    {
        rail.setTabs(navigator.groups(), navigator.isOpen() ? navigator.group() : -1);
        if (navigator.isOpen())
            rail.showTab(navigator.group());
    }

    @Override
    public void layout(BookGeometry geometry)
    {
        this.geometry = geometry;
        rail.setPosition(geometry.x(), geometry.y(), geometry.height());
    }

    @Override
    public BookRail rail()
    {
        return rail;
    }

    // Left-click a category: the item's recipes there. Right-click: the whole category.
    @Override
    public void selectTab(int index, int button)
    {
        if (button == 1)
            navigator.showAll(index);
        else
            navigator.selectGroup(index);
        rail.showTab(navigator.group());
    }

    @Override
    public int page()
    {
        return navigator.index();
    }

    @Override
    public int pages()
    {
        return navigator.size();
    }

    @Override
    public void step(int delta)
    {
        navigator.step(delta);
    }

    // Empty for recipes that make nothing, such as fuel and info pages.
    private List<BookIngredient> outputs()
    {
        return navigator.current().getRecipeSlotsView().getSlotViews(RecipeIngredientRole.OUTPUT).stream()
                .map(IRecipeSlotView::getDisplayedIngredient)
                .flatMap(Optional::stream)
                .map(BookIngredients::of)
                .flatMap(Optional::stream)
                .toList();
    }

    private RecipeViewport viewport(IRecipeLayoutDrawable<?> recipe)
    {
        recipe.setPosition(0, 0);
        Rect2i bounds = recipe.getRectWithBorder();
        return RecipeViewport.fit(geometry.body(), bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        rail.render(graphics, mouseX, mouseY);
        renderBack(graphics, mouseX, mouseY);
        tooltip = PendingTooltip.NONE;
        renderRecipe(graphics, mouseX, mouseY);

        // Seeing the recipe that makes an unlock is seeing the unlock, so the grid does not replay
        // its highlight later.
        List<BookIngredient> outputs = outputs();
        outputs.forEach(Progression::takeHighlight);
        renderBookmark(graphics, outputs, mouseX, mouseY);
    }

    // The search bar and its magnifier are baked into the book texture, so a slice of the book's
    // plain interior covers that strip and the back control sits on clean parchment.
    private void renderBack(GuiGraphics graphics, int mouseX, int mouseY)
    {
        Rect cover = geometry.searchCover();
        Canvas.texture(graphics, BACKGROUND, cover.x(), cover.y(), cover.width(), cover.height(),
                BookGeometry.BACK_X + 1, BookGeometry.GRID_Y + 1, BookGeometry.WIDTH - 2 * BookGeometry.BACK_X,
                cover.height(), 256, 256);

        Rect back = geometry.back();
        Canvas.sprite(graphics, BACK_SPRITES.get(true, back.contains(mouseX, mouseY)), back.x(), back.y(),
                BookGeometry.ARROW_W, BookGeometry.ARROW_H);
        graphics.drawString(Minecraft.getInstance().font, BACK_LABEL, back.x() + BookGeometry.ARROW_W + 3,
                back.y() + (BookGeometry.ARROW_H - 8) / 2, 0xFFFFFFFF, true);
    }

    private void renderRecipe(GuiGraphics graphics, int mouseX, int mouseY)
    {
        IRecipeLayoutDrawable<?> recipe = navigator.current();
        RecipeViewport view = viewport(recipe);
        double localX = view.localX(mouseX);
        double localY = view.localY(mouseY);

        BookRecipeRender.whileDrawing(place.missing(recipe), () ->
        {
            RecipeSlotUnderMouse slot = recipe.getSlotUnderMouse(localX, localY).orElse(null);
            Canvas.push(graphics);
            try
            {
                Canvas.translate(graphics, view.originX(), view.originY());
                Canvas.scale(graphics, view.scale());
                Canvas.translate(graphics, -view.boundsX(), -view.boundsY());
                recipe.drawRecipe(graphics, (int) localX, (int) localY);
                place.showMissing(graphics, recipe, (int) localX, (int) localY);
                // JEI's own overlays draw the slot tooltip at once, inside this scaled pose and with
                // the mod name stamped on it. Only its highlight is wanted here; the tooltip is taken
                // apart below and drawn later, unscaled, like every other tooltip in the book.
                if (slot == null)
                    recipe.drawOverlays(graphics, (int) localX, (int) localY);
                else
                    highlight(graphics, slot);
            }
            finally
            {
                Canvas.pop(graphics);
            }

            if (slot != null)
            {
                List<Component> lines = BookTooltips.whileBuilding(() -> RecipeTooltipFilter.filter(
                        BookIngredients.tooltip(slot.slot()), RecipePanel::translationKey));
                if (!lines.isEmpty())
                    tooltip = PendingTooltip.lines(() -> lines);
            }
        });
    }

    private static void highlight(GuiGraphics graphics, RecipeSlotUnderMouse slot)
    {
        Canvas.push(graphics);
        try
        {
            Canvas.translate(graphics, slot.offset().x(), slot.offset().y());
            JeiCompat.drawHighlight(slot.slot(), graphics);
        }
        finally
        {
            Canvas.pop(graphics);
        }
    }

    private static String translationKey(Component line)
    {
        return line.getContents() instanceof TranslatableContents contents ? contents.getKey() : "";
    }

    // A recipe with several outputs is bookmarked by its first.
    private void renderBookmark(GuiGraphics graphics, List<BookIngredient> outputs, int mouseX, int mouseY)
    {
        if (outputs.isEmpty())
            return;
        Rect button = geometry.bookmark(place.shown());
        boolean over = button.contains(mouseX, mouseY);
        boolean on = BookmarkStore.contains(outputs.get(0));
        ResourceLocation sprite = on ? (over ? BOOKMARK_ON_HL : BOOKMARK_ON) : (over ? BOOKMARK_OFF_HL : BOOKMARK_OFF);
        Canvas.sprite(graphics, sprite, button.x(), button.y(), button.width(), button.height());
        if (over)
            tooltip = PendingTooltip.of(on ? TOOLTIP_BOOKMARKED : TOOLTIP_BOOKMARK);
    }

    @Override
    public PendingTooltip tooltip()
    {
        return tooltip;
    }

    @Override
    public boolean click(double mouseX, double mouseY, int button, boolean doubleClick)
    {
        if (geometry.bookmark(place.shown()).contains(mouseX, mouseY))
        {
            List<BookIngredient> outputs = outputs();
            if (!outputs.isEmpty())
            {
                BookSounds.click();
                catalog.toggleBookmark(outputs.get(0));
                return true;
            }
        }
        if (geometry.back().contains(mouseX, mouseY))
        {
            BookSounds.click();
            close.run();
            return true;
        }
        return drillAt(mouseX, mouseY, button == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
    }

    // Clicking an ingredient inside the shown recipe drills into it: left shows its recipes, right
    // shows where it is used.
    private boolean drillAt(double mouseX, double mouseY, RecipeIngredientRole role)
    {
        IRecipeLayoutDrawable<?> recipe = navigator.current();
        RecipeViewport view = viewport(recipe);
        Optional<BookIngredient> clicked = recipe.getSlotUnderMouse(view.localX(mouseX), view.localY(mouseY))
                .map(RecipeSlotUnderMouse::slot)
                .flatMap(IRecipeSlotView::getDisplayedIngredient)
                .flatMap(BookIngredients::of);
        if (clicked.isPresent() && drill.test(clicked.get(), role))
        {
            BookSounds.click();
            return true;
        }
        return false;
    }
}
