package com.venice.venicehack.util;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.world.phys.AABB;

public final class RenderUtils {
    private RenderUtils() {}

    public static void lineBox(PoseStack pose, VertexConsumer vc, AABB b, float r, float g, float bl, float a) {
        PoseStack.Pose p = pose.last();
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        line(p, vc, x1, y1, z1, x2, y1, z1, r, g, bl, a); line(p, vc, x2, y1, z1, x2, y1, z2, r, g, bl, a);
        line(p, vc, x2, y1, z2, x1, y1, z2, r, g, bl, a); line(p, vc, x1, y1, z2, x1, y1, z1, r, g, bl, a);
        line(p, vc, x1, y2, z1, x2, y2, z1, r, g, bl, a); line(p, vc, x2, y2, z1, x2, y2, z2, r, g, bl, a);
        line(p, vc, x2, y2, z2, x1, y2, z2, r, g, bl, a); line(p, vc, x1, y2, z2, x1, y2, z1, r, g, bl, a);
        line(p, vc, x1, y1, z1, x1, y2, z1, r, g, bl, a); line(p, vc, x2, y1, z1, x2, y2, z1, r, g, bl, a);
        line(p, vc, x2, y1, z2, x2, y2, z2, r, g, bl, a); line(p, vc, x1, y1, z2, x1, y2, z2, r, g, bl, a);
    }

    private static void line(PoseStack.Pose p, VertexConsumer vc,
                             float x1, float y1, float z1, float x2, float y2, float z2,
                             float r, float g, float b, float a) {
        vc.addVertex(p, x1, y1, z1).setColor(r, g, b, a).setNormal(p, 0, 1, 0);
        vc.addVertex(p, x2, y2, z2).setColor(r, g, b, a).setNormal(p, 0, 1, 0);
    }

    public static void filledBox(PoseStack pose, AABB b, float r, float g, float bl, float a) {
        BufferBuilder buf = Tessellator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        PoseStack.Pose p = pose.last();
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        quad(buf, p, x1,y1,z1, x2,y1,z1, x2,y1,z2, x1,y1,z2, r,g,bl,a);
        quad(buf, p, x1,y2,z2, x2,y2,z2, x2,y2,z1, x1,y2,z1, r,g,bl,a);
        quad(buf, p, x1,y1,z1, x1,y2,z1, x2,y2,z1, x2,y1,z1, r,g,bl,a);
        quad(buf, p, x1,y1,z2, x2,y1,z2, x2,y2,z2, x1,y2,z2, r,g,bl,a);
        quad(buf, p, x1,y1,z1, x1,y1,z2, x1,y2,z2, x1,y2,z1, r,g,bl,a);
        quad(buf, p, x2,y1,z2, x2,y1,z1, x2,y2,z1, x2,y2,z2, r,g,bl,a);
        BufferUploader.drawWithShader(buf.buildOrThrow());
    }

    private static void quad(BufferBuilder buf, PoseStack.Pose p,
                             float ax,float ay,float az, float bx,float by,float bz,
                             float cx,float cy,float cz, float dx,float dy,float dz,
                             float r,float g,float b,float a) {
        buf.addVertex(p, ax, ay, az).setColor(r, g, b, a);
        buf.addVertex(p, bx, by, bz).setColor(r, g, b, a);
        buf.addVertex(p, cx, cy, cz).setColor(r, g, b, a);
        buf.addVertex(p, dx, dy, dz).setColor(r, g, b, a);
    }
}
