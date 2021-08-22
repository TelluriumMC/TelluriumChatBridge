package net.telluriummc.telluriumchatbridge.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.telluriummc.telluriumchatbridge.TelluriumChatBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "sendSystemMessage", at = @At("HEAD"))
    public void sendMessage(Text text, UUID uuid, CallbackInfo ci) {
        String parsedUuid = uuid.toString().replaceAll("-", "");
        String rawMessage = text.asString();

        if (rawMessage.equals("")) {
            return;
        }

        TelluriumChatBridge.discordBot.handleMinecraftMessage(rawMessage, parsedUuid);
    }
}
