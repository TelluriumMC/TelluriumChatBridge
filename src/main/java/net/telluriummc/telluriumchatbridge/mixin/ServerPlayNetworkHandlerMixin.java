package net.telluriummc.telluriumchatbridge.mixin;

import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.telluriummc.telluriumchatbridge.TelluriumChatBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "handleMessage", at = @At("HEAD"), cancellable = true)
    public void handleMessage(TextStream.Message message, CallbackInfo info) {
        String uuid = player.getUuidAsString().replaceAll("-", "");
        String rawMessage = message.getRaw();
        TelluriumChatBridge.discordBot.handleMinecraftMessage(rawMessage, uuid);
    }
}
