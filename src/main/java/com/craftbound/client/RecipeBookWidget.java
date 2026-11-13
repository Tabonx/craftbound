package com.craftbound.client;

import java.util.List;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.BookRecipes;
import com.craftbound.client.jei.RecipeGroup;
import com.craftbound.client.progression.Progression;

import mezz.jei.api.recipe.RecipeIngredientRole;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

// The recipe book as a real screen widget, so it renders in order and receives clicks, scroll and
// typed characters. A shell over its two states, browsing and reading a recipe: it lays them out,
// draws the page and the pager they share, and hands each the input that lands on it. Every hit
// test is answered from what the book shows now, never from what the last frame drew.
public final class RecipeBookWidget extends LooseWidget
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.withDefaultNamespace("textures/gui/recipe_book.png");
    private static final WidgetSprites FORWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_forward_highlighted"));
    private static final WidgetSprites BACKWARD_SPRITES = new WidgetSprites(
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward"),
            ResourceLocation.withDefaultNamespace("recipe_book/page_backward_highlighted"));
    // The book texture is plain between its search strip and its bottom border, so a taller book
    // repeats a slice from there.
    private static final int STRETCH_Y = 80;

    private final BookState state;
    private final PlaceControl place = new PlaceControl();
    private final BrowsePanel browse;
    private final RecipePanel recipe;
    private final ImageButton backButton;
    private final ImageButton forwardButton;
    private BookGeometry geometry;
    private int right;
    private int top;
    // The toggle, when the screen has no place for it.
    private AbstractWidget carried = null;

    RecipeBookWidget(BookSession session, BookState state, BookCatalog catalog)
    {
        super(0, 0, BookGeometry.WIDTH, BookGeometry.HEIGHT, Component.translatable("craftbound.recipebook.title"));
        this.state = state;
        browse = new BrowsePanel(session, state, catalog, this::open);
        recipe = new RecipePanel(state.recipes, catalog, place, this::showRecipes, this::closeRecipe);
        backButton = new ImageButton(0, 0, BookGeometry.ARROW_W, BookGeometry.ARROW_H, BACKWARD_SPRITES,
                b -> panel().step(-1));
        forwardButton = new ImageButton(0, 0, BookGeometry.ARROW_W, BookGeometry.ARROW_H, FORWARD_SPRITES,
                b -> panel().step(1));
        relayout();
    }

    public boolean isSearchFocused()
    {
        return visible && browse.search().isFocused();
    }

    public void blurSearch()
    {
        browse.search().setFocused(false);
    }

    public void bind(MenuBinding binding)
    {
        place.bind(binding.placer());
        state.craftable.bind(binding.craftable(), binding.version());
    }

    public void carry(AbstractWidget toggle)
    {
        carried = toggle;
        relayout();
    }

    // Puts the browsing book's right edge at `right`. The book grows left from there to show a recipe.
    public void anchor(int right, int top, int height)
    {
        this.right = right;
        this.top = top;
        setHeight(height);
        relayout();
    }

    private BookPanel panel()
    {
        return state.recipes.isOpen() ? recipe : browse;
    }

    private void relayout()
    {
        geometry = BookGeometry.anchored(right, top, getHeight(), state.recipes.isOpen());
        super.setPosition(geometry.x(), geometry.y());
        setWidth(geometry.width());
        browse.layout(geometry);
        recipe.layout(geometry);
        place.layout(geometry);
        backButton.setPosition(geometry.backArrow().x(), geometry.backArrow().y());
        forwardButton.setPosition(geometry.forwardArrow().x(), geometry.forwardArrow().y());
        if (carried != null)
            carried.setPosition(geometry.carriedX(), geometry.carriedY(carried.getHeight()));
    }

    // Advances the shown recipe's own animations, JEI's cycling through the items an ingredient
    // stands for included. Holding shift stops the cycle, which is JEI's own behaviour.
    public void tick()
    {
        if (!visible)
            return;
        state.ticks++;
        browse.tick();
        place.tick();
        if (state.recipes.isOpen())
            state.recipes.current().tick();
    }

    // Brings what the book shows up to date with the world, and the controls with what it shows.
    private void sync()
    {
        browse.refresh();
        place.show(state.recipes.isOpen() ? state.recipes.current() : null);
        BookPanel panel = panel();
        boolean paged = panel.pages() > 1;
        backButton.visible = paged && panel.page() > 0;
        forwardButton.visible = paged && panel.page() < panel.pages() - 1;
    }

    // OUTPUT shows how the ingredient is made; INPUT shows where it is used.
    private boolean showRecipes(BookIngredient ingredient, RecipeIngredientRole role)
    {
        List<RecipeGroup> groups = BookRecipes.groupsFor(ingredient, role, Progression.gate());
        if (groups.isEmpty())
            return false;
        browse.leave();
        state.recipes.open(groups, place.menuGroup(groups));
        recipe.syncRail();
        relayout();
        return true;
    }

    private void closeRecipe()
    {
        state.recipes.close();
        recipe.syncRail();
        relayout();
    }

    // Left-click an item: how it is made. Right-click: where it is used. Shift-left-click places it
    // straight into the grid, or opens it like a plain click when nothing can be placed.
    private void open(BookIngredient item, int button)
    {
        if (button == 1 || !Input.shiftDown() || !place.quickPlace(item))
            showRecipes(item, button == 1 ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT);
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        // An open variants panel takes every click, so nothing else may light up as if it would.
        int panelMouseX = mouseX;
        int panelMouseY = mouseY;
        if (browse.variantsOpen())
        {
            mouseX = -1;
            mouseY = -1;
        }

        sync();
        state.highlights.advance(partialTick);
        drawBackground(graphics);
        BookPanel panel = panel();
        // Tabs are drawn over the book, like vanilla's, their edge overlapping its left border.
        panel.render(graphics, mouseX, mouseY, partialTick);
        place.render(graphics, mouseX, mouseY, partialTick);
        renderPager(graphics, panel, mouseX, mouseY, partialTick);
        if (panel == browse)
            browse.renderVariants(graphics, panelMouseX, panelMouseY);
    }

    // The panel texture is stretched horizontally to the current width (identity while browsing) and
    // grows taller by repeating its plain middle.
    private void drawBackground(GuiGraphics graphics)
    {
        int x = getX();
        int y = getY();
        int half = BookGeometry.HEIGHT / 2;
        Canvas.texture(graphics, BACKGROUND, x, y, getWidth(), half, 1, 1, BookGeometry.WIDTH, half, 256, 256);
        if (getHeight() > BookGeometry.HEIGHT)
            Canvas.texture(graphics, BACKGROUND, x, y + half, getWidth(), getHeight() - BookGeometry.HEIGHT,
                    1, 1 + STRETCH_Y, BookGeometry.WIDTH, 1, 256, 256);
        Canvas.texture(graphics, BACKGROUND, x, y + getHeight() - half, getWidth(), half,
                1, 1 + half, BookGeometry.WIDTH, half, 256, 256);
    }

    private void renderPager(GuiGraphics graphics, BookPanel panel, int mouseX, int mouseY, float partialTick)
    {
        Canvas.widget(backButton, graphics, mouseX, mouseY, partialTick);
        Canvas.widget(forwardButton, graphics, mouseX, mouseY, partialTick);
        if (panel.pages() <= 1)
            return;
        var font = Minecraft.getInstance().font;
        String label = (panel.page() + 1) + "/" + panel.pages();
        graphics.drawString(font, label, getX() + getWidth() / 2 - font.width(label) / 2, geometry.pagerY() + 5,
                0xFFFFFFFF, true);
    }

    // Drawn after the whole screen (slots and their placeholder sprites) so the tooltip layers
    // don't interleave with the inventory's empty-slot icons.
    public void renderDeferredTooltip(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (!visible)
            return;
        var font = Minecraft.getInstance().font;
        BookRail.Tab tab = panel().rail().hovered();
        if (tab != null)
            Canvas.tooltip(graphics, font, tab.title(), mouseX, mouseY);
        else
            panel().tooltip().draw(graphics, font, mouseX, mouseY);
    }

    // Clicks reach the book from the host before the screen sees them (see click), never as a child
    // of the screen, which would offer them to its own widgets first.
    @Override
    protected boolean mousePressed(double mouseX, double mouseY, int button, boolean doubleClick)
    {
        return false;
    }

    public boolean click(double mouseX, double mouseY, int button, boolean doubleClick)
    {
        if (!visible)
            return false;
        sync();
        if (browse.clickVariants(mouseX, mouseY, button))
            return true;
        if (place.click(mouseX, mouseY, button))
            return true;

        BookPanel panel = panel();
        if (railClicked(panel, mouseX, mouseY, button))
            return true;
        if (Input.click(backButton, mouseX, mouseY, button) || Input.click(forwardButton, mouseX, mouseY, button))
        {
            blurSearch();
            return true;
        }
        return panel.click(mouseX, mouseY, button, doubleClick) || isMouseOverBook(mouseX, mouseY);
    }

    private static boolean railClicked(BookPanel panel, double mouseX, double mouseY, int button)
    {
        BookRail rail = panel.rail();
        int arrow = rail.arrowAt(mouseX, mouseY);
        if (arrow != 0)
        {
            BookSounds.click();
            rail.scroll(arrow);
            return true;
        }
        int tab = rail.tabAt(mouseX, mouseY);
        if (tab < 0)
            return false;
        BookSounds.click();
        panel.selectTab(tab, button);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        if (!visible)
            return false;
        BookRail rail = panel().rail();
        if (rail.isOver(mouseX, mouseY))
        {
            rail.scroll(scrollY < 0 ? 1 : -1);
            return true;
        }
        if (!isMouseOverBook(mouseX, mouseY))
            return false;
        browse.closeVariants();
        panel().step(scrollY < 0 ? 1 : -1);
        return true;
    }

    @Override
    protected boolean typed(int codePoint, int modifiers)
    {
        return visible && Input.charTyped(browse.search(), codePoint, modifiers);
    }

    @Override
    protected boolean keyDown(int keyCode, int scanCode, int modifiers)
    {
        if (!visible)
            return false;
        if (keyCode == 256) // let escape close the screen rather than being swallowed
            return false;
        if (Input.keyPressed(browse.search(), keyCode, scanCode, modifiers))
            return true;
        return browse.search().canConsumeInput(); // swallow other keys while the search box is focused
    }

    // The book's clickable area is the page plus the rail beside it. Newer versions hand a click
    // only to the widget this reports as being under the cursor, so leaving the rail out of it makes
    // the ribbons unclickable.
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return visible && (isMouseOverBook(mouseX, mouseY) || panel().rail().isOver(mouseX, mouseY));
    }

    private boolean isMouseOverBook(double mouseX, double mouseY)
    {
        return geometry.bounds().contains(mouseX, mouseY);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        defaultButtonNarrationText(output);
    }
}
