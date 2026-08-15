package com.craftbound.client;

import com.craftbound.Craftbound;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.upgrade.UnbindLensPayload;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.neoforge.network.PacketDistributor;

// The recipe-book toggle beside the crafting grid: vanilla's button, with the lens laid over it
// once the book carries the upgrade, so the upgraded book is visible before it is even opened.
// Shift + right-click pries the lens back out. Left alone otherwise: no tooltip, so the button
// behaves exactly as vanilla's does.
public final class RecipeBookToggleButton extends ImageButton
{
    public static final int WIDTH = 20;
    public static final int HEIGHT = 18;

    private static final ResourceLocation UPGRADE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "recipe_book/book_upgrade");

    public RecipeBookToggleButton(int x, int y, Button.OnPress onPress)
    {
        super(x, y, WIDTH, HEIGHT, RecipeBookComponent.RECIPE_BUTTON_SPRITES, onPress, CommonComponents.EMPTY);
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderWidget(graphics, mouseX, mouseY, partialTick);

        if (ClientBookUpgrade.hintsActive())
            graphics.blitSprite(UPGRADE_OVERLAY, getX(), getY(), WIDTH, HEIGHT);
    }

    // Shift + right-click takes the lens back, which only the server can do; the book is only ever
    // bound on a server that has Craftbound, so the payload always has a channel to travel on. The
    // modifier is there because losing the upgrade to a stray click would be a poor surprise.
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button != 1 || !Screen.hasShiftDown() || !visible || !isMouseOver(mouseX, mouseY)
                || !ClientBookUpgrade.bound())
            return super.mouseClicked(mouseX, mouseY, button);

        PacketDistributor.sendToServer(new UnbindLensPayload());
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        return true;
    }

    // A mouse click otherwise leaves the button focused, so it stays highlighted until focus moves
    // elsewhere. Refuse focus so only a live hover highlights the toggle.
    @Override
    public void setFocused(boolean focused)
    {
        super.setFocused(false);
    }
}
