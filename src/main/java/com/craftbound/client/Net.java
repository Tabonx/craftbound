package com.craftbound.client;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.PacketDistributor;
//? if >=1.21.5 {
/*import net.neoforged.neoforge.client.network.ClientPacketDistributor;
*///?}

// Sending to the server moved off the shared packet distributor and onto a client-only one.
public final class Net
{
    public static void toServer(CustomPacketPayload payload)
    {
        //? if >=1.21.5 {
        /*ClientPacketDistributor.sendToServer(payload);
        *///?} else {
        PacketDistributor.sendToServer(payload);
        //?}
    }

    private Net() {}
}
