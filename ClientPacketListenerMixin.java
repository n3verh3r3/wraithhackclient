package com.venice.venicehack.mixin;

import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.modules.AntiBanModules;
import com.venice.venicehack.modules.CombatModules;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void onMotion(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || packet.getId() != mc.player.getId()) return;
        if (!CombatModules.Velocity.active) return;
        CombatModules.Velocity v = (CombatModules.Velocity) VeniceHack.modules.getByName("Velocity");
        if (v == null) return;
        mc.player.setDeltaMovement(packet.getXa() * v.h(), packet.getYa() * v.v(), packet.getZa() * v.h());
        ci.cancel();
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    private void onSystemChat(ClientboundSystemChatPacket packet, CallbackInfo ci) {
        AntiBanModules.lastChat = packet.content().getString();
    }

    @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
    private void onSendChat(String message, CallbackInfo ci) {
        if (VeniceHack.commands.handle(message)) ci.cancel();
    }
}
