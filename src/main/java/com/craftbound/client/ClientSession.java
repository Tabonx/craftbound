package com.craftbound.client;

import java.util.function.Supplier;

import com.craftbound.client.book.BookCatalog;
import com.craftbound.client.book.BookState;
import com.craftbound.client.bookmarks.Bookmarks;
import com.craftbound.client.jei.JeiSession;
import com.craftbound.client.jei.RecipeIndexBuilder;
import com.craftbound.client.jei.RecipeIndexSnapshot;
import com.craftbound.client.progression.ClientHeldVariants;
import com.craftbound.client.progression.ClientObtainedItems;
import com.craftbound.client.progression.ProgressionCache;
import com.craftbound.client.progression.ProgressionToasts;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldScopedStore;
import com.craftbound.client.upgrade.ClientBookUpgrade;
import com.craftbound.obtained.PlayerItems;
import com.craftbound.progression.ProgressionConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

// Everything the client keeps for one connection to a world. It begins at login and ends at logout,
// so nothing one world taught the client can leak into the next.
public final class ClientSession
{
    private static ClientSession current = null;

    private final ClientStores stores =
            new ClientStores(Minecraft.getInstance().gameDirectory.toPath().resolve("config"));
    private final ClientObtainedItems obtained = new ClientObtainedItems(stores);
    private final ClientHeldVariants heldVariants = new ClientHeldVariants(stores);
    private final ClientBookUpgrade bookUpgrade = new ClientBookUpgrade();
    private final Supplier<WorldScopedStore<Bookmarks>> bookmarks =
            stores.lazy("craftbound-bookmarks.json", Bookmarks.CODEC, Bookmarks::new);
    private final BookCatalog bookCatalog = new BookCatalog();
    private final ScreenScoped<BookState> bookStates = new ScreenScoped<>(BookState::new);
    private final ProgressionCache progression = new ProgressionCache(obtained::current, heldVariants::current,
            ProgressionConfig::rules, () -> RecipeIndexBuilder.current().map(RecipeIndexSnapshot::index));

    // Begun on first use as well, so state the server sends while the client logs in has a home.
    public static ClientSession current()
    {
        if (current == null)
            current = new ClientSession();
        return current;
    }

    static void begin()
    {
        current();
    }

    static void end()
    {
        if (current != null)
            current.stores.close();
        current = null;
    }

    public ClientObtainedItems obtained()
    {
        return obtained;
    }

    public ClientBookUpgrade bookUpgrade()
    {
        return bookUpgrade;
    }

    public ProgressionCache progression()
    {
        return progression;
    }

    public BookCatalog bookCatalog()
    {
        return bookCatalog;
    }

    public BookState bookState(Object screen)
    {
        return bookStates.of(screen);
    }

    public static void screenClosed(Object screen)
    {
        if (current != null)
            current.bookStates.closed(screen);
    }

    public WorldScopedStore<Bookmarks> bookmarks()
    {
        return bookmarks.get();
    }

    void tick(LocalPlayer player)
    {
        obtained.tick(player);
        heldVariants.tick(player);
        if (player.tickCount % PlayerItems.SCAN_INTERVAL_TICKS == 0 && JeiSession.running())
            ProgressionToasts.announce(progression);
        stores.tick(player.tickCount);
    }
}
