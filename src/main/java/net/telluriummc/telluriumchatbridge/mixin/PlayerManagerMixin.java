package net.telluriummc.telluriumchatbridge.mixin;

import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(PlayerManager.class)
abstract class PlayerManagerMixin {
    @Inject(method = "broadcastChatMessage", at = @At("HEAD"), cancellable = true)
    private void broadcastChatMessage(Text text, MessageType messageType, UUID uuid, CallbackInfo info) {
        //TelluriumChatBridge.discordBot.handleMinecraftMessage(text, messageType, uuid, info);
    }
}