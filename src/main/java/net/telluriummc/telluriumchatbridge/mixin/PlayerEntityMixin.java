package net.telluriummc.telluriumchatbridge.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
abstract class PlayerEntityMixin implements EntityLike {
    @Inject(method = "getDisplayName()Lnet/minecraft/text/Text;", at = @At("HEAD"), require = 1, cancellable = true)
    private void InjectMethod(CallbackInfoReturnable<Text> callbackInfo) {

    }
}