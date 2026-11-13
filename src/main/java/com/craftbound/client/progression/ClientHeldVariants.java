package com.craftbound.client.progression;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.craftbound.client.jei.BookIngredients;
import com.craftbound.client.jei.JeiSession;
import com.craftbound.client.storage.ClientStores;
import com.craftbound.client.storage.WorldKey;
import com.craftbound.client.storage.WorldScopedStore;
import com.craftbound.obtained.PlayerItems;
import com.craftbound.progression.UnlockKey;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;

// Which variants of an item the player has held, such as which potion. The server records item ids
// only, since it has no JEI to say which components make a variant, so the client tracks these
// itself, per world, from its own view of the inventory.
//
// A variant's key holds JEI's subtype, which is only stable within one session, so what is saved is
// one stack of each variant, and the keys are worked out again from those whenever JEI starts. A
// stack that does not decode, such as one from a mod that is gone, stays in the file as it was.
public final class ClientHeldVariants
{
    static final Codec<List<Either<ItemStack, Dynamic<?>>>> CODEC =
            Codec.either(ItemStack.CODEC, Codec.PASSTHROUGH).listOf().xmap(ArrayList::new, List::copyOf);

    private final Supplier<WorldScopedStore<List<Either<ItemStack, Dynamic<?>>>>> store;
    private List<Either<ItemStack, Dynamic<?>>> keysSource = null;
    private int keysGeneration = -1;
    private Set<String> keys = Set.of();

    public ClientHeldVariants(ClientStores stores)
    {
        this.store = stores.lazy("craftbound-held-variants.json", CODEC, ArrayList::new);
    }

    public Set<String> current()
    {
        if (Minecraft.getInstance().player == null || !JeiSession.running())
            return Set.of();

        List<Either<ItemStack, Dynamic<?>>> stacks = stacks();
        if (stacks != keysSource || keysGeneration != JeiSession.generation())
        {
            keys = new HashSet<>();
            for (Either<ItemStack, Dynamic<?>> stack : stacks)
                stack.left().flatMap(ClientHeldVariants::variantKey).ifPresent(keys::add);
            keysSource = stacks;
            keysGeneration = JeiSession.generation();
        }
        return keys;
    }

    public void tick(LocalPlayer player)
    {
        if (player.tickCount % PlayerItems.SCAN_INTERVAL_TICKS != 0 || !JeiSession.running())
            return;

        Set<String> known = current();
        for (ItemStack stack : PlayerItems.carried(player))
        {
            Optional<String> key = variantKey(stack);
            if (key.isPresent() && known.add(key.get()))
            {
                stacks().add(Either.left(stack.copyWithCount(1)));
                store.get().markDirty();
            }
        }
    }

    // Present only for stacks JEI tells apart from the rest of their item.
    private static Optional<String> variantKey(ItemStack stack)
    {
        return BookIngredients.unlockKeyOf(stack)
                .filter(key -> !key.equals(UnlockKey.withoutSubtype(key)));
    }

    private List<Either<ItemStack, Dynamic<?>>> stacks()
    {
        return store.get().open(WorldKey.current(), ClientStores::registryOps);
    }
}
