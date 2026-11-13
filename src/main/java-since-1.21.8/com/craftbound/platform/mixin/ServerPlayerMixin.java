package com.craftbound.platform.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.craftbound.platform.LegacyPlayerData;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin
{
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void craftbound$readLegacyAttachments(ValueInput input, CallbackInfo ci)
    {
        LegacyPlayerData.restore((ServerPlayer) (Object) this, input);
    }
}
