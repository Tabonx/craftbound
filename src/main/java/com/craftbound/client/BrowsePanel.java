package com.craftbound.client;

import java.util.List;
import java.util.function.ObjIntConsumer;

import com.craftbound.client.jei.BookIngredient;
import com.craftbound.client.jei.JeiSearch;
import com.craftbound.client.progression.Progression;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;

// The browsing state: a search field, the craftable filter, the ribbons choosing what fills the grid,
// the grid itself, and the panel listing an entry's variants. Uses a vanilla EditBox for search so it
// inherits its look and keys.
final class BrowsePanel implements BookPanel
{
    private static final ResourceLocation FILTER_ENABLED =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled");
    private static final ResourceLocation FILTER_DISABLED =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled");
    private static final ResourceLocation FILTER_ENABLED_HL =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_enabled_highlighted");
    private static final ResourceLocation FILTER_DISABLED_HL =
            ResourceLocation.withDefaultNamespace("recipe_book/filter_disabled_highlighted");
    private static final Component TOOLTIP_ALL = Component.translatable("gui.recipebook.toggleRecipes.all");
    private static final Component TOOLTIP_CRAFTABLE = Component.translatable("gui.recipebook.toggleRecipes.craftable");
    // How long an entry with variants shows each one, vanilla's own pace.
    private static final int VARIANT_TICKS = 30;
    private static final int ITEM_INSET = 4;

    private final BookState state;
    private final BookCatalog catalog;
    private final ObjIntConsumer<BookIngredient> open;
    private final EditBox search;
    private final BookRail rail = new BookRail();
    private final VariantsOverlay variants = new VariantsOverlay();
    private List<BrowseTab> tabs = List.of();
    private BookGeometry geometry = BookGeometry.anchored(BookGeometry.WIDTH, 0, BookGeometry.HEIGHT, false);
    private PendingTooltip tooltip = PendingTooltip.NONE;

    // `open` is told which item was clicked, and with which button.
    BrowsePanel(BookState state, BookCatalog catalog, ObjIntConsumer<BookIngredient> open)
    {
        this.state = state;
        this.catalog = catalog;
        this.open = open;
        search = new EditBox(Minecraft.getInstance().font, 0, 0, BookGeometry.SEARCH_W, BookGeometry.SEARCH_H,
                Component.translatable("itemGroup.search"));
        search.setMaxLength(50);
        search.setHint(Component.translatable("gui.recipebook.search_hint"));
        search.setValue(JeiSearch.query());
        search.setResponder(this::searchChanged);
        syncTabs();
    }

    @Override
    public void layout(BookGeometry geometry)
    {
        this.geometry = geometry;
        search.setPosition(geometry.search().x(), geometry.search().y());
        rail.setPosition(geometry.x(), geometry.y(), geometry.height());
        state.pager.fit(geometry.height());
    }

    EditBox search()
    {
        return search;
    }

    // Another search synced with JEI, such as the stock keeper's, may have changed it meanwhile.
    void tick()
    {
        String shared = JeiSearch.query();
        if (!search.isFocused() && !shared.equals(search.getValue()))
            search.setValue(shared);
    }

    // Fills the grid again when what it is filled from moved: the catalog, what is unlocked, or what
    // can be made. The last waits while the variants panel is open, since Create's stock changes all
    // the time and would close it under the cursor.
    void refresh()
    {
        int revision = catalog.refresh();
        Progression.refresh();
        boolean stale = revision != state.catalogRevision || Progression.version() != state.unlocksVersion;
        if (revision != state.catalogRevision)
        {
            state.catalogRevision = revision;
            syncTabs();
        }
        state.unlocksVersion = Progression.version();
        if (!variants.isOpen() && state.craftable.refresh() && filtering())
            stale = true;
        if (stale)
            refilter();
    }

    // The bookmarks ribbon closes the rail once there are any.
    private void syncTabs()
    {
        if (catalog.bookmarked().isEmpty() && state.tab == BrowseTab.BOOKMARKS)
            state.tab = BrowseTab.ALL;
        tabs = BrowseTab.visible(!catalog.bookmarked().isEmpty());
        rail.setTabs(tabs, tabs.indexOf(state.tab));
    }

    private void searchChanged(String text)
    {
        JeiSearch.setQuery(text);
        refilter();
    }

    private void refilter()
    {
        state.entries = new BrowseQuery(state.tab, search.getValue(), filtering()).entries(catalog,
                item -> item.item().map(state.craftable::contains).orElse(false));
        state.pager.fill(state.entries.size(), state.tab);
        variants.close();
    }

    private boolean filtering()
    {
        return state.craftable.bound() && RecipeBookState.isFiltering();
    }

    private void toggleFilter()
    {
        RecipeBookState.toggleFiltering();
        if (filtering())
            state.craftable.reload();
        refilter();
    }

    void leave()
    {
        variants.close();
        search.setFocused(false);
    }

    boolean variantsOpen()
    {
        return variants.isOpen();
    }

    void closeVariants()
    {
        variants.close();
    }

    @Override
    public BookRail rail()
    {
        return rail;
    }

    @Override
    public void selectTab(int index, int button)
    {
        state.tab = tabs.get(index);
        rail.showTab(index);
        refilter();
    }

    @Override
    public int page()
    {
        return state.pager.page();
    }

    @Override
    public int pages()
    {
        return state.pager.pageCount();
    }

    @Override
    public void step(int delta)
    {
        state.pager.step(delta);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        rail.render(graphics, mouseX, mouseY);
        Canvas.widget(search, graphics, mouseX, mouseY, partialTick);
        tooltip = PendingTooltip.NONE;

        Rect filter = geometry.filter();
        if (state.craftable.bound())
        {
            boolean over = filter.contains(mouseX, mouseY);
            boolean on = RecipeBookState.isFiltering();
            ResourceLocation sprite = on
                    ? (over ? FILTER_ENABLED_HL : FILTER_ENABLED)
                    : (over ? FILTER_DISABLED_HL : FILTER_DISABLED);
            Canvas.sprite(graphics, sprite, filter.x(), filter.y(), filter.width(), filter.height());
            if (over)
                tooltip = PendingTooltip.of(on ? TOOLTIP_CRAFTABLE : TOOLTIP_ALL);
        }

        GridPager pager = state.pager;
        for (int i = 0; i < pager.shown(); i++)
            renderCell(graphics, state.entries.get(pager.first() + i),
                    geometry.gridX() + GridPager.cellX(i), geometry.gridY() + GridPager.cellY(i));

        int hovered = pager.cellAt(mouseX - geometry.gridX(), mouseY - geometry.gridY());
        if (hovered >= 0)
            tooltip = itemTooltip(shown(state.entries.get(pager.first() + hovered)));
    }

    // Drawn last, over the pager too, and the only thing hovered while it is open, as in vanilla.
    void renderVariants(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (!variants.isOpen())
            return;
        variants.render(graphics, (variant, slotX, slotY) -> renderCell(graphics, List.of(variant), slotX, slotY));
        BookIngredient hovered = variants.variantAt(mouseX, mouseY);
        tooltip = hovered == null ? PendingTooltip.NONE : itemTooltip(hovered);
    }

    private static PendingTooltip itemTooltip(BookIngredient item)
    {
        return PendingTooltip.lines(() ->
        {
            TooltipFlag flag = Minecraft.getInstance().options.advancedItemTooltips
                    ? TooltipFlag.ADVANCED
                    : TooltipFlag.NORMAL;
            return BookTooltips.whileBuilding(() -> item.tooltip(flag));
        });
    }

    @Override
    public PendingTooltip tooltip()
    {
        return tooltip;
    }

    // Which variant an entry shows right now: they take turns, as in vanilla's book.
    private BookIngredient shown(List<BookIngredient> entry)
    {
        return entry.get(state.ticks / VARIANT_TICKS % entry.size());
    }

    // Every variant's highlight is claimed, so one swell stands for the lot.
    private void renderCell(GuiGraphics graphics, List<BookIngredient> entry, int cellX, int cellY)
    {
        String key = entry.get(0).unlockKey();
        boolean fresh = false;
        for (BookIngredient variant : entry)
            fresh |= Progression.takeHighlight(variant);
        if (fresh)
            state.highlights.start(key);

        boolean swelling = state.highlights.active(key);
        if (swelling)
            Canvas.scaleAbout(graphics, cellX + ITEM_INSET + 8, cellY + ITEM_INSET + 8, state.highlights.scale(key));
        try
        {
            Canvas.sprite(graphics, slotFor(entry), cellX, cellY, GridPager.CELL, GridPager.CELL);
            shown(entry).render(graphics, cellX + ITEM_INSET, cellY + ITEM_INSET);
        }
        finally
        {
            if (swelling)
                Canvas.pop(graphics);
        }
    }

    // With nothing to check against, no entry is marked as one the player cannot make.
    private ResourceLocation slotFor(List<BookIngredient> entry)
    {
        boolean craftable = !state.craftable.bound()
                || entry.stream().anyMatch(item -> item.item().map(state.craftable::contains).orElse(false));
        return SlotSprites.of(entry.size() > 1, entry.stream().anyMatch(Progression::unlocksMore), craftable);
    }

    // Like vanilla's overlay, an open variants panel takes the click wherever it lands and closes.
    boolean clickVariants(double mouseX, double mouseY, int button)
    {
        if (!variants.isOpen())
            return false;
        BookIngredient variant = variants.variantAt(mouseX, mouseY);
        variants.close();
        if (variant != null)
            open.accept(variant, button);
        return true;
    }

    @Override
    public boolean click(double mouseX, double mouseY, int button, boolean doubleClick)
    {
        boolean onSearch = geometry.search().contains(mouseX, mouseY);
        search.setFocused(onSearch);
        if (onSearch)
        {
            // Right-click clears, as in the creative inventory.
            if (button == 1)
                search.setValue("");
            else
                Input.click(search, mouseX, mouseY, button, doubleClick);
            return true;
        }

        if (state.craftable.bound() && geometry.filter().contains(mouseX, mouseY))
        {
            BookSounds.click();
            toggleFilter();
            return true;
        }

        int index = state.pager.cellAt(mouseX - geometry.gridX(), mouseY - geometry.gridY());
        if (index < 0)
            return false;
        List<BookIngredient> entry = state.entries.get(state.pager.first() + index);
        if (entry.size() > 1)
            variants.open(entry, geometry.gridX() + GridPager.cellX(index), geometry.gridY() + GridPager.cellY(index),
                    geometry.x() + BookGeometry.WIDTH / 2, geometry.y() + 13 + geometry.height() / 2);
        else
            open.accept(entry.get(0), button);
        return true;
    }
}
