package com.venice.venicehack.ui;

import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.core.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HudManager {
    private final Minecraft mc = Minecraft.getInstance();

    public void render(GuiGraphicsExtractor gfx) {
        if (mc.player == null) return;

        gfx.text(mc.font, Component.literal("VeniceHack v" + VeniceHack.VERSION), 4, 4, 0xFFD7B3FF, true);

        String pos = String.format("%.1f, %.1f, %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ());
        gfx.text(mc.font, Component.literal(mc.getFps() + " fps | " + ping() + " ms"), 4, 16, 0xFFAAAAAA, true);
        gfx.text(mc.font, Component.literal("XYZ " + pos), 4, 27, 0xFFAAAAAA, true);

        List<Module> on = new ArrayList<>();
        for (Module m : VeniceHack.modules.getModules()) if (m.isEnabled()) on.add(m);
        on.sort(Comparator.comparingInt(m -> -mc.font.width(m.getName())));

        int y = 4;
        for (Module m : on) {
            int w = mc.font.width(m.getName());
            int x = mc.getWindow().getGuiScaledWidth() - w - 4;
            gfx.fill(x - 3, y - 1, mc.getWindow().getGuiScaledWidth(), y + 9, 0x88000000);
            gfx.text(mc.font, Component.literal(hue(y * 12) + m.getName()), x, y, 0xFFFFFFFF, true);
            y += 10;
        }

        StaffOverlay.render(gfx);
        VeniceHack.notifications.render(gfx);
    }

    private int ping() {
        if (mc.player == null || mc.getConnection() == null) return 0;
        var e = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        return e != null ? e.getLatency() : 0;
    }

    private String hue(int offset) {
        float h = (System.currentTimeMillis() % 6000) / 6000f * 360f + offset;
        int rgb = Color.HSBtoRGB(h / 360f, 0.7f, 1f) & 0xFFFFFF;
        String hx = String.format("%06X", rgb);
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hx.toCharArray()) sb.append('§').append(c);
        return sb.toString();
    }
}
