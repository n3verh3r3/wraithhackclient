package com.venice.venicehack.mixin;

import com.venice.venicehack.modules.MovementModules;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (MovementModules.NoSlow.active && self.isUsingItem() && !self.isPassenger()) {
            Vec3 v = self.getDeltaMovement();
            self.setDeltaMovement(v.x * 5.0, v.y, v.z * 5.0);
        }
    }

    @Inject(method = "sendPosition", at = @At("HEAD"))
    private void beforeSendPosition(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        if (MovementModules.NoFall.active) self.setOnGround(true);
    }
}
