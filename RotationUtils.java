package com.venice.venicehack.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public final class RotationUtils {
    private RotationUtils() {}
    private static final Minecraft mc = Minecraft.getInstance();

    public static float[] lookAt(Vec3 target) {
        Vec3 eye = mc.player.getEyePosition();
        double dx = target.x - eye.x, dy = target.y - eye.y, dz = target.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }

    public static void face(Vec3 target, boolean instant) {
        float[] r = lookAt(target);
        if (instant) { mc.player.setYRot(r[0]); mc.player.setXRot(r[1]); }
        else {
            mc.player.setYRot(approach(mc.player.getYRot(), r[0], 35f));
            mc.player.setXRot(approach(mc.player.getXRot(), r[1], 35f));
        }
    }

    public static void faceEntity(Entity e, boolean instant) {
        face(e.position().add(0, e.getBbHeight() / 2.0, 0), instant);
    }

    private static float approach(float from, float to, float step) {
        float diff = Mth.wrapDegrees(to - from);
        if (Math.abs(diff) <= step) return to;
        return from + Math.signum(diff) * step;
    }
}
