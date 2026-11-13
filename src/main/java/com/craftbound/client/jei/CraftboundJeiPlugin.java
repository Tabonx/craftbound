package com.craftbound.client.jei;

import com.craftbound.Craftbound;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public final class CraftboundJeiPlugin implements IModPlugin
{
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(Craftbound.MODID, "jei");

    @Override
    public ResourceLocation getPluginUid()
    {
        return ID;
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addGlobalGuiHandler(new JeiOverlayHider());
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime)
    {
        JeiSession.start(jeiRuntime);
    }

    @Override
    public void onRuntimeUnavailable()
    {
        JeiSession.stop();
    }
}
