package com.venice.venicehack.util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
public final class ChatUtils {
    private ChatUtils() {}
    public static void send(String msg) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        mc.player.displayClientMessage(Component.literal("[VH] " + msg), false);
    }
}
