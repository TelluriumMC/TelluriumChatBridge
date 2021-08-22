package net.telluriummc.telluriumchatbridge.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.telluriummc.telluriumchatbridge.TelluriumChatBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        String uuid = player.getUuidAsString().replaceAll("-", "");
        TelluriumChatBridge.discordBot.handleMinecraftMessage(source.getDeathMessage(player).getString().replace("_", "\\_"), uuid);
    }
}
