package com.craftbound.client.jei;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.compat.jei.CreateJEI;

import mezz.jei.api.IModPlugin;
import mezz.jei.common.Internal;
import mezz.jei.common.gui.textures.Textures;
import mezz.jei.common.input.IInternalKeyMappings;
import mezz.jei.common.network.IConnectionToServer;
import mezz.jei.common.network.packets.PlayToServerPacket;
import mezz.jei.common.util.MinecraftLocaleSupplier;
import mezz.jei.common.util.Translator;
import mezz.jei.gui.config.InternalKeyMappings;
import mezz.jei.gui.overlay.bookmarks.IngredientsTooltipComponent;
import mezz.jei.gui.overlay.bookmarks.PreviewTooltipComponent;
import mezz.jei.library.gui.ingredients.TagContentTooltipComponent;
import mezz.jei.library.plugins.vanilla.VanillaPlugin;
import mezz.jei.library.startup.JeiStarter;
import mezz.jei.library.startup.StartData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RecipesUpdatedEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

public final class CraftboundRecipeRuntime
{
    private static final IConnectionToServer NO_SERVER_CONNECTION = new IConnectionToServer()
    {
        @Override
        public boolean isJeiOnServer()
        {
            return false;
        }

        @Override
        public <T extends PlayToServerPacket<T>> void sendPacketToServer(T packet)
        {
        }
    };

    private final JeiStarter starter;
    private final RecipeRuntimeReadiness readiness = new RecipeRuntimeReadiness();
    private Connection connection;

    private CraftboundRecipeRuntime()
    {
        Translator.setLocaleSupplier(new MinecraftLocaleSupplier());

        IInternalKeyMappings keyMappings = new InternalKeyMappings(key -> {});
        Internal.setKeyMappings(keyMappings);
        Internal.setServerConnection(NO_SERVER_CONNECTION);

        List<IModPlugin> plugins = new ArrayList<>(List.of(
                new VanillaPlugin(),
                new CreateJEI(),
                new CraftboundJeiPlugin()));
        starter = new JeiStarter(new StartData(plugins, NO_SERVER_CONNECTION, keyMappings));
    }

    public static void register(IEventBus modEventBus)
    {
        CraftboundRecipeRuntime runtime = new CraftboundRecipeRuntime();
        IEventBus gameEventBus = net.neoforged.neoforge.common.NeoForge.EVENT_BUS;
        gameEventBus.addListener(EventPriority.NORMAL, false, TagsUpdatedEvent.class, runtime::onTagsUpdated);
        gameEventBus.addListener(EventPriority.NORMAL, false, RecipesUpdatedEvent.class, runtime::onRecipesUpdated);
        gameEventBus.addListener(EventPriority.NORMAL, false, ClientPlayerNetworkEvent.LoggingOut.class,
                runtime::onLoggingOut);
        modEventBus.addListener(runtime::onRegisterReloadListeners);
        modEventBus.addListener(runtime::onRegisterTooltipFactories);
    }

    private void onTagsUpdated(TagsUpdatedEvent event)
    {
        observeConnection();
        if (connection != null)
            apply(readiness.tagsReady());
    }

    private void onRecipesUpdated(RecipesUpdatedEvent event)
    {
        observeConnection();
        if (connection != null)
            apply(readiness.recipesReady());
    }

    private void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event)
    {
        if (readiness.reset())
            starter.stop();
        connection = null;
    }

    private void onRegisterReloadListeners(RegisterClientReloadListenersEvent event)
    {
        Textures textures = Internal.getTextures();
        event.registerReloadListener(textures.getSpriteUploader());
    }

    private void onRegisterTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(IngredientsTooltipComponent.class, component -> component);
        event.register(PreviewTooltipComponent.class, component -> component);
        event.register(TagContentTooltipComponent.class, component -> component);
    }

    private void observeConnection()
    {
        Connection current = currentConnection();
        if (current == connection)
            return;

        if (readiness.reset())
            starter.stop();
        connection = current;
    }

    private void apply(RecipeRuntimeReadiness.Action action)
    {
        if (action == RecipeRuntimeReadiness.Action.RESTART)
            starter.stop();
        if (action != RecipeRuntimeReadiness.Action.NONE)
            starter.start();
    }

    private static Connection currentConnection()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientPacketListener packetListener = minecraft.getConnection();
        if (packetListener != null)
            return packetListener.getConnection();
        if (minecraft.pendingConnection != null)
            return minecraft.pendingConnection;
        if (minecraft.screen instanceof ConnectScreen connectScreen)
            return connectScreen.connection;
        return null;
    }
}
