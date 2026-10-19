package com.venice.venicehack.ui;

import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.modules.AntiBanModules;
import com.venice.venicehack.util.ChatUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public final class StaffOverlay {
    private StaffOverlay() {}
    public static final Map<UUID, PlayerInfo> STAFF = new LinkedHashMap<>();
    public static final Map<UUID, Boolean> VANISHED = new HashMap<>();
    public static final Set<UUID> WATCHING = new HashSet<>();
    private static final Set<UUID> WAS_WATCHING = new HashSet<>();
    private static final Minecraft mc = Minecraft.getInstance();

    public static void render(GuiGraphicsExtractor gfx) {
        AntiBanModules.StaffDetector mod = (AntiBanModules.StaffDetector) VeniceHack.modules.getByName("StaffDetector");
        if (mod == null || !mod.isEnabled() || STAFF.isEmpty()) return;

        int right = mc.getWindow().getGuiScaledWidth() - 4;
        int x = right - 128;
        int y = 42, rowH = 22;

        gfx.fill(x - 6, y - 14, right, y + STAFF.size() * rowH + 4, 0xAA000000);
        gfx.text(mc.font, Component.literal("STAFF ONLINE (" + STAFF.size() + ")"), x, y - 12, 0xFFFF6666, true);
        y += 2;

        WATCHING.clear();
        for (PlayerInfo info : STAFF.values()) {
            String name = info.getProfile().getName();
            boolean vanished = VANISHED.getOrDefault(info.getProfile().getId(), false);
            boolean watching = isWatching(info);
            if (watching) WATCHING.add(info.getProfile().getId());

            ResourceLocation skin = skin(info);
            if (skin != null) {
                gfx.fill(x, y, x + 16, y + 16, 0xFF333333);
                gfx.blit(RenderPipelines.GUI_TEXTURED, skin, x, y, 8, 8, 8, 8, 8, 8, 64, 64);
            }

            String tag = (vanished ? " [v]" : "") + (watching ? " [WATCHING]" : "");
            gfx.text(mc.font, Component.literal(name + tag), x + 20, y + 4,
                    watching ? 0xFFFF6666 : 0xFFFFDD55, true);

            if (watching && !WAS_WATCHING.contains(info.getProfile().getId())) {
                ChatUtils.send(name + " is watching you!");
                if (mc.player != null) mc.player.playSound(SoundEvents.NOTE_BLOCK_BELL.value(), 1f, 1.8f);
            }
            WAS_WATCHING.add(info.getProfile().getId());
            y += rowH;
        }
    }

    private static boolean isWatching(PlayerInfo info) {
        if (mc.player == null || mc.level == null) return false;
        var e = mc.level.getPlayerByUUID(info.getProfile().getId());
        if (e == null || e == mc.player) return false;
        if (mc.player.distanceTo(e) > 40) return false;
        Vec3 toYou = mc.player.position().add(0, mc.player.getBbHeight() / 2.0, 0)
                .subtract(e.position().add(0, e.getBbHeight() / 2.0, 0)).normalize();
        return toYou.dot(e.getViewVector(1f)) > 0.93;
    }

    private static ResourceLocation skin(PlayerInfo info) {
        if (mc.level == null) return null;
        var p = mc.level.getPlayerByUUID(info.getProfile().getId());
        if (p instanceof AbstractClientPlayer acp) return acp.getSkin().texture();
        try { return mc.getSkinManager().getInMemorySkin(info).texture(); }
        catch (Throwable t) { return null; }
    }
}
