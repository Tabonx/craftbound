package com.craftbound.client;

import java.util.Optional;

import com.craftbound.Craftbound;
import com.craftbound.client.mixin.ContainerScreenAccessor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

// On every recipe-book screen (player inventory, crafting table, furnace, smoker, blast furnace),
// swap the vanilla recipe-book toggle for our own (same sprite, same spot), add Craftbound's book
// widget beside the GUI, and shift the GUI aside to make room while the book is open. Keyed off the
// menu being a RecipeBookMenu, so it follows wherever vanilla would have offered a recipe book.
@EventBusSubscriber(modid = Craftbound.MODID, value = Dist.CLIENT)
public final class RecipeBookScreenTweaks
{
    private RecipeBookScreenTweaks()
    {
    }

    @SubscribeEvent
    public static void onInit(ScreenEvent.Init.Post event)
    {
        if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)
                || !(screen.getMenu() instanceof RecipeBookMenu<?, ?> menu))
            return;

        ImageButton vanillaButton = findRecipeButton(event);
        if (vanillaButton == null)
            return;

        // Capture the vanilla toggle's placement (relative to the GUI's left edge) so our own sits
        // exactly where it did, on this screen and any other crafting screen alike.
        int buttonOffsetX = vanillaButton.getX() - screen.getGuiLeft();
        int buttonY = vanillaButton.getY();
        event.removeListener(vanillaButton);

        RecipeBookWidget book = new RecipeBookWidget();
        book.setCraftableSource(() -> CraftableItems.craftableIn(menu));
        book.setPlacer(new RecipePlacer(menu));
        event.addListener(book);

        ImageButton button = new ImageButton(
                vanillaButton.getX(), buttonY, 20, 18,
                RecipeBookComponent.RECIPE_BUTTON_SPRITES,
                b -> {
                    RecipeBookState.toggle();
                    applyLayout(screen, book, b, buttonOffsetX, buttonY);
                })
        {
            // A mouse click otherwise leaves the button focused, so it stays highlighted until
            // focus moves elsewhere. Refuse focus so only a live hover highlights the toggle.
            @Override
            public void setFocused(boolean focused)
            {
                super.setFocused(false);
            }
        };
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
        if (Minecraft.getInstance().screen instanceof AbstractContainerScreen<?> screen)
            book(screen).ifPresent(RecipeBookWidget::tick);
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
        book.setPosition(RecipeBookLayout.bookRight(leftPos) - RecipeBookWidget.WIDTH, screen.getGuiTop());
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
