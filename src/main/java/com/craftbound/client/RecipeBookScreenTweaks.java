package com.craftbound.client;

import java.util.Optional;

import com.craftbound.Craftbound;
import com.craftbound.client.jei.CraftboundJeiPlugin;
import com.craftbound.client.jei.TransferPlacer;
import com.craftbound.client.mixin.ContainerScreenAccessor;
//? if create {
import com.craftbound.client.create.StockKeeperPlacer;
import com.craftbound.client.create.StockKeeperStock;
//?}

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.RecipeBookMenu;
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
// swap the vanilla recipe-book toggle for our own (same sprite, same spot), add Craftbound's book
// widget beside the GUI, and shift the GUI aside to make room while the book is open. Keyed off the
// menu being a RecipeBookMenu, so it follows wherever vanilla would have offered a recipe book.
//
// A screen that takes recipes from JEI but has no recipe book, like Create's stock keeper, gets the
// book too, with the toggle just outside its left edge. JEI would show its ingredient list there.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class RecipeBookScreenTweaks
{
    //? if create {
    // Guarded so the Create classes are only ever resolved when Create is installed.
    private static final boolean CREATE_LOADED = ModList.get().isLoaded("create");
    //?}

    private RecipeBookScreenTweaks()
    {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen))
            return;
        if (screen.getMenu() instanceof RecipeBookMenu menu)
            replaceVanillaBook(event, screen, menu);
        else if (docksBeside(screen))
            addBeside(event, screen);
    }

    public static boolean docksBeside(AbstractContainerScreen<?> screen)
    {
        return !(screen.getMenu() instanceof RecipeBookMenu) && CraftboundJeiPlugin.acceptsRecipes(screen.getMenu());
    }

    // The screen placed what it draws when it opened, so it opens again to move aside for the book.
    private static void addBeside(ScreenEvent.Init.Post event, AbstractContainerScreen<?> screen)
    {
        int toggleX = left(screen) - RecipeBookLayout.TOGGLE_SPACE;
        RecipeBookWidget book = new RecipeBookWidget();
        book.visible = RecipeBookState.isOpen();
        book.setPlacer(new TransferPlacer(screen.getMenu()));
        //? if create {
        if (CREATE_LOADED && StockKeeperStock.isStockKeeper(screen.getMenu()))
        {
            book.setPlacer(new StockKeeperPlacer(new TransferPlacer(screen.getMenu()), screen.getMenu()));
            book.setCraftableSource(() -> StockKeeperStock.craftable(screen.getMenu()),
                    () -> StockKeeperStock.version(screen.getMenu()));
        }
        //?}
        book.setPosition(RecipeBookLayout.besideBookRight(left(screen)) - RecipeBookWidget.WIDTH, top(screen));
        event.addListener(book);
        event.addListener(new RecipeBookToggleButton(toggleX, top(screen), b ->
        {
            RecipeBookState.toggle();
            Screens.rebuild(screen);
        }));
    }

    private static void replaceVanillaBook(ScreenEvent.Init.Post event, AbstractContainerScreen<?> screen,
            RecipeBookMenu menu)
    {
        ImageButton vanillaButton = findRecipeButton(event);
        if (vanillaButton == null)
            return;

        // Capture the vanilla toggle's placement (relative to the GUI's left edge) so our own sits
        // exactly where it did, on this screen and any other crafting screen alike.
        int buttonOffsetX = vanillaButton.getX() - left(screen);
        int buttonY = vanillaButton.getY();
        event.removeListener(vanillaButton);

        RecipeBookWidget book = new RecipeBookWidget();
        book.setCraftableSource(() -> CraftableItems.craftableIn(menu), RecipeBookScreenTweaks::inventoryVersion);
        book.setPlacer(new GridPlacer(menu));
        event.addListener(book);

        RecipeBookToggleButton button = new RecipeBookToggleButton(vanillaButton.getX(), buttonY,
                b -> {
                    RecipeBookState.toggle();
                    applyLayout(screen, book, b, buttonOffsetX, buttonY);
                });
        event.addListener(button);

        applyLayout(screen, book, button, buttonOffsetX, buttonY);
    }

    // Draw the book's item tooltip last, so it sits above the GUI's slot placeholders.
    @SubscribeEvent
    public static void onRender(ScreenEvent.Render.Post event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
            book(screen).ifPresent(book ->
                    book.renderDeferredTooltip(event.getGuiGraphics(), event.getMouseX(), event.getMouseY()));
    }

    // Widgets are not ticked by their screen, and the shown recipe needs it: JEI cycles ingredients
    // that stand for several items (any planks, any log) on a tick, and stands still without one.
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        if (Screens.current() instanceof AbstractContainerScreen<?> screen)
            book(screen).ifPresent(RecipeBookWidget::tick);
    }

    // While its search has focus the book takes the keyboard first. Some screens, the stock keeper
    // among them, hand typed keys to their own fields and never to the widgets beside them.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onKeyPressed(ScreenEvent.KeyPressed.Pre event)
    {
        searchingBook(event.getScreen()).ifPresent(book ->
        {
            if (Input.keyPressed(book, event.getKeyCode(), event.getScanCode(), event.getModifiers()))
                event.setCanceled(true);
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCharTyped(ScreenEvent.CharacterTyped.Pre event)
    {
        searchingBook(event.getScreen()).ifPresent(book ->
        {
            if (Input.charTyped(book, (char) event.getCodePoint(), 0))
                event.setCanceled(true);
        });
    }

    // The stock keeper scrolls its own list wherever the cursor is, so the book takes the wheel first
    // while it is under it.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event)
    {
        if (event.getScreen() instanceof AbstractContainerScreen<?> screen)
            book(screen).filter(book -> book.mouseScrolled(event.getMouseX(), event.getMouseY(),
                            event.getScrollDeltaX(), event.getScrollDeltaY()))
                    .ifPresent(book -> event.setCanceled(true));
    }

    // Such a screen may also keep a click on its own field to itself, so the book lets go of the
    // keyboard on any click it is not under.
    @SubscribeEvent
    public static void onMouseClicked(ScreenEvent.MouseButtonPressed.Pre event)
    {
        searchingBook(event.getScreen())
                .filter(book -> !book.isMouseOver(event.getMouseX(), event.getMouseY()))
                .ifPresent(RecipeBookWidget::blurSearch);
    }

    public static boolean isSearching(Screen screen)
    {
        return searchingBook(screen).isPresent();
    }

    private static Optional<RecipeBookWidget> searchingBook(Screen screen)
    {
        return screen instanceof AbstractContainerScreen<?> container
                ? book(container).filter(RecipeBookWidget::isSearchFocused)
                : Optional.empty();
    }

    private static int inventoryVersion()
    {
        var player = Minecraft.getInstance().player;
        return player == null ? -1 : player.getInventory().getTimesChanged();
    }

    private static Optional<RecipeBookWidget> book(AbstractContainerScreen<?> screen)
    {
        for (GuiEventListener listener : screen.children())
        {
            if (listener instanceof RecipeBookWidget book)
                return Optional.of(book);
        }
        return Optional.empty();
    }

    // Position the GUI, our button and the book for the current open/closed state.
    private static void applyLayout(AbstractContainerScreen<?> screen, RecipeBookWidget book,
            Button button, int buttonOffsetX, int buttonY)
    {
        boolean open = RecipeBookState.isOpen();
        ContainerScreenAccessor accessor = (ContainerScreenAccessor) screen;
        int leftPos = RecipeBookLayout.inventoryLeftPos(
                screen.width, accessor.craftbound$getImageWidth(), open);
        accessor.craftbound$setLeftPos(leftPos);

        button.setPosition(leftPos + buttonOffsetX, buttonY);

        book.visible = open;
        book.setPosition(RecipeBookLayout.bookRight(leftPos) - RecipeBookWidget.WIDTH, top(screen));
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
