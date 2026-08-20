package com.craftbound;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

// A server player's own level. Newer versions have level() answer as a ServerLevel and dropped the
// narrower accessor, which is too common a name to rewrite across the whole source.
public final class ServerLevels
{
    public static ServerLevel of(ServerPlayer player)
    {
        //? if >=1.21.8 {
        /*return player.level();
        *///?} else {
        return player.serverLevel();
        //?}
    }

    private ServerLevels() {}
}
