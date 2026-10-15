package com.craftbound.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.craftbound.Craftbound;
import com.craftbound.client.RecipeBookLayout.Placement;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.TransferPlacer;
import com.craftbound.client.mixin.ContainerScreenAccessor;
//? if create {
import com.craftbound.client.create.StockKeeperPlacer;
import com.craftbound.client.create.StockKeeperStock;
//?}

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.Slot;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
//? if create {
import net.neoforged.fml.ModList;
//?}
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// On every recipe-book screen (player inventory, crafting table, furnace, smoker, blast furnace),
// swap the vanilla recipe-book toggle for our own (same sprite, same spot) and add Craftbound's book
// beside the GUI. Keyed off the menu being a RecipeBookMenu, so it follows wherever vanilla would
// have offered a recipe book. Such a screen places itself through its vanilla book, which
// RecipeBookComponentMixin answers for.
//
// A screen that takes recipes from JEI but has no recipe book, like Create's stock keeper, gets the
// book too, on the side where JEI would show its ingredient list. Its toggle sits left of the screen
// while the book is closed, and the open book carries it.
// ContainerScreenLayoutMixin moves it aside.
//
// Where the window is too narrow for both, the book hides the screen, as vanilla's does.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class RecipeBookScreenTweaks
{
    //? if create {
    // Guarded so the Create classes are only ever resolved when Create is installed.
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    //?}

    // What each kind of screen draws past its left edge, learned the first time it is shown. The
    // screen is laid out before any of that exists, so a first sighting lays it out again.
    private static final Map<Class<?>, SideAreas> SIDES = new HashMap<>();
    private static final int INSIDE_TOGGLE_X = 5;
    private static Attached attached;
    private static Screen relayoutPending;
    private static Screen relaidOut;
    // A click the book took, whose release the screen must not see either: with an item on the
    // cursor, a release outside the screen's own bounds throws the item out.
    private static boolean pressTaken;

    private RecipeBookScreenTweaks()
    {
    }

    // The book on the open screen, and where it was put.
    private static final class Attached
    {
        final AbstractContainerScreen<?> screen;
        final RecipeBookWidget book;
        final RecipeBookToggleButton toggle;
        final Placement placement;
        final boolean overlay;
        boolean measured;

        Attached(AbstractContainerScreen<?> screen, RecipeBookWidget book, RecipeBookToggleButton toggle,
                Placement placement, boolean overlay)
        {
            this.screen = screen;
            this.book = book;
            this.toggle = toggle;
            this.placement = placement;
            this.overlay = overlay;
        }

        // The book takes a click before the screen does, and anywhere in its own column: the screen
        // would hand it to its own widgets first, or read it as a click outside itself.
        boolean press(double mouseX, double mouseY, int button, boolean doubleClick)
        {
            if (toggle.isMouseOver(mouseX, mouseY))
            {
                Input.click(toggle, mouseX, mouseY, button);
                return true;
            }
            if (book.visible && (book.click(mouseX, mouseY, button, doubleClick) || owns(mouseX, mouseY)))
                return true;
            return overlay;
        }

        private boolean owns(double mouseX, double mouseY)
        {
            int left = book.getX() - RecipeBookLayout.RAIL_WIDTH;
            int right = placement.bookRight() + RecipeBookLayout.GAP;
            return mouseX >= left && mouseX < right
                    && mouseY >= placement.bookTop() && mouseY < placement.bookTop() + placement.bookHeight();
        }
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen))
            return;
        attached = null;
        if (hosted(screen))
            attachHosted(event, screen);
        else if (docksBeside(screen))
            attach(event, screen, null);
    }

    private static boolean hosted(AbstractContainerScreen<?> screen)
    {
        return screen.getMenu() instanceof RecipeBookMenu;
    }

    public static boolean docksBeside(AbstractContainerScreen<?> screen)
    {
        return !hosted(screen) && CraftboundJeiPlugin.acceptsRecipes(screen.getMenu());
    }

    // Asked by a recipe-book screen placing itself, which is always the one being opened.
    public static int hostedLeftPos(int width, int imageWidth)
    {
        int gap = Screens.current() instanceof AbstractContainerScreen<?> screen ? gap(screen) : RecipeBookLayout.GAP;
        return RecipeBookLayout.leftPos(width, imageWidth, gap, RecipeBookState.isOpen());
    }

    // The width a screen beside the book is told it has, so that centering itself moves it aside.
    // Taken from the window: a screen rebuilt without a resize keeps the width it was given.
    public static int besideScreenWidth(AbstractContainerScreen<?> screen, int imageWidth)
    {
        return RecipeBookLayout.screenWidthFor(Minecraft.getInstance().getWindow().getGuiScaledWidth(), imageWidth,
                gap(screen), RecipeBookState.isOpen());
    }

    // Everything between the book and the screen's left edge. The toggle is never in it: an open book
    // with nowhere else for the toggle carries it.
    private static int gap(AbstractContainerScreen<?> screen)
    {
        return RecipeBookLayout.GAP + sides(screen).reach();
    }

    // A recipe-book screen keeps the toggle left of its grid. A vanilla screen with that corner free
    // of slots, such as the brewing stand, carries it there too, 3 px lower to clear the anvil's
    // hammer. A modded screen's art is unknown, so its toggle stays outside where it covers nothing.
    private static boolean togglesInside(AbstractContainerScreen<?> screen)
    {
        if (!screen.getClass().getName().startsWith("net.minecraft."))
            return false;
        int y = insideToggleY(screen);
        for (Slot slot : screen.getMenu().slots)
        {
            if (slot.x < INSIDE_TOGGLE_X + RecipeBookToggleButtonBase.WIDTH + 1 && slot.x + 17 > INSIDE_TOGGLE_X
                    && slot.y < y + RecipeBookToggleButtonBase.HEIGHT + 1 && slot.y + 17 > y)
                return false;
        }
        return true;
    }

    private static int insideToggleY(AbstractContainerScreen<?> screen)
    {
        return imageHeight(screen) / 2 - 46;
    }

    private static SideAreas sides(Screen screen)
    {
        return SIDES.getOrDefault(screen.getClass(), SideAreas.NONE);
    }

    private static void attachHosted(ScreenEvent.Init.Post event, AbstractContainerScreen<?> screen)
    {
        ImageButton vanillaButton = findRecipeButton(event);
        if (vanillaButton == null)
        {
            // A mod took the toggle away, and the book with it. The screen made room for the book
            // all the same, so it is put back in the middle.
            ((ContainerScreenAccessor) screen).craftbound$setLeftPos((screen.width - imageWidth(screen)) / 2);
            return;
        }
        event.removeListener(vanillaButton);
        attach(event, screen, vanillaButton);
    }

    private static void attach(ScreenEvent.Init.Post event, AbstractContainerScreen<?> screen, ImageButton vanillaButton)
    {
        var window = Minecraft.getInstance().getWindow();
        boolean open = RecipeBookState.isOpen();
        int gap = gap(screen);
        Placement placement = RecipeBookLayout.place(window.getGuiScaledWidth(), window.getGuiScaledHeight(),
                left(screen), top(screen), imageWidth(screen), imageHeight(screen), gap, sides(screen).floor());
        boolean overlay = open && placement.overlay();

        RecipeBookWidget book = new RecipeBookWidget();
        book.visible = open;
        bind(book, screen);
        book.place(placement.bookRight(), placement.bookTop(), placement.bookHeight());

        RecipeBookToggleButton toggle = new RecipeBookToggleButton(0, 0, b ->
        {
            RecipeBookState.toggle();
            Screens.rebuild(screen);
        });
        if (vanillaButton != null && !overlay)
            toggle.setPosition(vanillaButton.getX(), vanillaButton.getY());
        else if (togglesInside(screen) && !overlay)
            toggle.setPosition(left(screen) + INSIDE_TOGGLE_X, top(screen) + insideToggleY(screen));
        else if (open)
            book.carry(toggle);
        else
            toggle.setPosition(Math.max(0, left(screen) - sides(screen).reach() - RecipeBookLayout.TOGGLE_SPACE),
                    top(screen));

        event.addListener(book);
        event.addListener(toggle);
        attached = new Attached(screen, book, toggle, placement, overlay);
    }

    private static void bind(RecipeBookWidget book, AbstractContainerScreen<?> screen)
    {
        if (screen.getMenu() instanceof RecipeBookMenu menu)
        {
            book.setCraftableSource(() -> CraftableItems.craftableIn(menu), RecipeBookScreenTweaks::inventoryVersion);
            book.setPlacer(new GridPlacer(menu));
            return;
        }
        book.setPlacer(new TransferPlacer(screen.getMenu()));
        //? if create {
        if (CREATE_LOADED && StockKeeperStock.isStockKeeper(screen.getMenu()))
        {
            book.setPlacer(new StockKeeperPlacer(new TransferPlacer(screen.getMenu()), screen.getMenu()));
            book.setCraftableSource(() -> StockKeeperStock.craftable(screen.getMenu()),
                    () -> StockKeeperStock.version(screen.getMenu()));
        }
        //?}
    }

    private static Optional<Attached> attached(Screen screen)
    {
        return attached != null && attached.screen == screen ? Optional.of(attached) : Optional.empty();
    }

    // With the book over it, the screen is not drawn at all, only what lies behind it.
    @SubscribeEvent
    public static void onRenderPre(ScreenEvent.Render.Pre event)
    {
        attached(event.getScreen()).filter(host -> host.overlay).ifPresent(host ->
        {
            event.setCanceled(true);
            ((ContainerScreenAccessor) host.screen).craftbound$setHoveredSlot(null);
            var graphics = event.getGuiGraphics();
            int mouseX = event.getMouseX();
            int mouseY = event.getMouseY();
            Canvas.backdrop(host.screen, graphics, mouseX, mouseY, event.getPartialTick());
            Canvas.widget(host.book, graphics, mouseX, mouseY, event.getPartialTick());
            Canvas.widget(host.toggle, graphics, mouseX, mouseY, event.getPartialTick());
            host.book.renderDeferredTooltip(graphics, mouseX, mouseY);
            Canvas.drawTooltips(graphics, mouseX, mouseY, event.getPartialTick());
        });
    }

    // Draw the book's item tooltip last, so it sits above the GUI's slot placeholders.
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event)
    {
        attached(event.getScreen()).ifPresent(host ->
        {
            if (!host.overlay)
            {
                host.book.renderDeferredTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY());
                Canvas.drawTooltips(event.getGuiGraphics(), event.getMouseX(), event.getMouseY(), event.getPartialTick());
            }
            learnSides(host);
        });
    }

    // Read once the screen has drawn, when its widgets exist and the mods reporting around it know
    // where they are.
    private static void learnSides(Attached host)
    {
        if (host.measured || !CraftboundJeiPlugin.hasRuntime())
            return;
        host.measured = true;
        AbstractContainerScreen<?> screen = host.screen;
        List<SideAreas.Area> areas = CraftboundJeiPlugin.exclusionAreas(screen).stream()
                .map(area -> new SideAreas.Area(area.getX(), area.getY(), area.getWidth(), area.getHeight()))
                .toList();
        SideAreas found = SideAreas.of(areas, left(screen), top(screen), imageHeight(screen));
        SideAreas used = sides(screen);
        SIDES.put(screen.getClass(), found);
        // Once per screen, in case what a mod reports moves with the screen itself.
        if (!found.equals(used) && RecipeBookState.isOpen() && relaidOut != screen)
            relayoutPending = screen;
    }

    // Widgets are not ticked by their screen, and the shown recipe needs it: JEI cycles ingredients
    // that stand for several items (any planks, any log) on a tick, and stands still without one.
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        Screen screen = Screens.current();
        if (relayoutPending != null)
        {
            if (relayoutPending == screen)
            {
                relaidOut = screen;
                Screens.rebuild(screen);
            }
            relayoutPending = null;
        }
        attached(screen).ifPresent(host -> host.book.tick());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMousePressed(ScreenEvent.MouseButtonPressed.Pre event)
    {
        attached(event.getScreen()).ifPresent(host ->
        {
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            // Some screens keep a click on their own field to themselves, so the book lets go of the
            // keyboard on any click it is not under.
            if (!host.book.isMouseOver(mouseX, mouseY))
                host.book.blurSearch();
            if (host.press(mouseX, mouseY, event.getButton(), doubleClick(event)))
            {
                pressTaken = true;
                event.setCanceled(true);
            }
        });
    }

    private static boolean doubleClick(ScreenEvent.MouseButtonPressed.Pre event)
    {
        //? if >=1.21.10 {
        /*return event.isDoubleClick();
        *///?} else {
        return false;
        //?}
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseReleased(ScreenEvent.MouseButtonReleased.Pre event)
    {
        if (pressTaken)
        {
            pressTaken = false;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseDragged(ScreenEvent.MouseDragged.Pre event)
    {
        if (pressTaken)
            event.setCanceled(true);
    }

    // A screen that closes under a click never sees its release.
    @SubscribeEvent
    public static void onOpening(ScreenEvent.Opening event)
    {
        pressTaken = false;
    }

    // The stock keeper scrolls its own list wherever the cursor is, so the book takes the wheel first
    // while it is under it. A screen hidden behind the book takes none.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event)
    {
        attached(event.getScreen())
                .filter(host -> host.book.mouseScrolled(event.getMouseX(), event.getMouseY(),
                        event.getScrollDeltaX(), event.getScrollDeltaY()) || host.overlay)
                .ifPresent(host -> event.setCanceled(true));
    }

    // While its search has focus the book takes the keyboard first. Some screens, the stock keeper
    // among them, hand typed keys to their own fields and never to the widgets beside them.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event)
    {
        searchingBook(event.getScreen()).ifPresent(host ->
        {
            if (Input.keyPressed(host, event.getKeyCode(), event.getScanCode(), event.getModifiers()))
                event.setCanceled(true);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event)
    {
        searchingBook(event.getScreen()).ifPresent(host ->
        {
            if (Input.charTyped(host, (char) event.getCodePoint(), 0))
                event.setCanceled(true);
        });
    }

    public static boolean isSearching(Screen screen)
    {
        return searchingBook(screen).isPresent();
    }

    private static Optional<RecipeBookWidget> searchingBook(Screen screen)
    {
        return attached(screen).map(host -> host.book).filter(RecipeBookWidget::isSearchFocused);
    }

    private static int inventoryVersion()
    {
        var player = Minecraft.getInstance().player;
        return player == null ? -1 : player.getInventory().getTimesChanged();
    }

    private static int left(AbstractContainerScreen<?> screen)
    {
        //? if >=26.1.2 {
        /*return screen.getLeftPos();
        *///?} else {
        return screen.getGuiLeft();
        //?}
    }

    private static int top(AbstractContainerScreen<?> screen)
    {
        //? if >=26.1.2 {
        /*return screen.getTopPos();
        *///?} else {
        return screen.getGuiTop();
        //?}
    }

    private static int imageWidth(AbstractContainerScreen<?> screen)
    {
        return ((ContainerScreenAccessor) screen).craftbound$getImageWidth();
    }

    private static int imageHeight(AbstractContainerScreen<?> screen)
    {
        return ((ContainerScreenAccessor) screen).craftbound$getImageHeight();
    }

    // The recipe-book toggle is the only 20x18 ImageButton these screens add.
    private static ImageButton findRecipeButton(ScreenEvent.Init.Post event)
    {
        for (GuiEventListener listener : event.getListenersList())
        {
            if (listener instanceof ImageButton button
                    && button.getWidth() == 20 && button.getHeight() == 18)
                return button;
        }
        return null;
    }
}
