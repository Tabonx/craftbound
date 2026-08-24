package com.craftbound.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;

// Sending to the server later moved off the shared packet distributor and onto a client-only one.
public final class Net
{
    public static void toServer(CustomPacketPayload payload)
    {
        PacketDistributor.sendToServer(payload);
    }

    private Net() {}
}
