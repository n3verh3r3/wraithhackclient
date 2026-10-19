package com.venice.venicehack.util;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PlayerUtils {
    private PlayerUtils() {}
    private static final Minecraft mc = Minecraft.getInstance();

    public static List<LivingEntity> targets(double range, boolean playersOnly) {
        List<LivingEntity> out = new ArrayList<>();
        if (mc.player == null || mc.level == null) return out;
        for (Entity e : mc.level.entitiesForRendering()) {
            if (!(e instanceof LivingEntity le) || e == mc.player) continue;
            if (playersOnly && !(e instanceof Player)) continue;
            if (mc.player.distanceTo(e) <= range) out.add(le);
        }
        out.sort(Comparator.comparingDouble(mc.player::distanceTo));
        return out;
    }

    public static AABB box(Entity e, double expand) { return e.getBoundingBox().inflate(expand); }

    public static int findItem(Item item) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 36; i++) {
            var s = mc.player.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() == item) return i;
        }
        return -1;
    }

    public static int invToMenuSlot(int invIndex) { return invIndex < 9 ? invIndex + 36 : invIndex; }
}
