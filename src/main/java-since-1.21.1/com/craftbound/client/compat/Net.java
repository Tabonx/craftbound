package com.craftbound.client.compat;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

public final class Net
{
    public static void toServer(CustomPacketPayload payload)
    {
        PacketDistributor.sendToServer(payload);
    }

    private Net() {}
}
