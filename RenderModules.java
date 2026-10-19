package com.venice.venicehack.modules;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import com.venice.venicehack.util.RenderUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class RenderModules {
    private RenderModules() {}

    public static class ESP extends Module {
        private final ModeSetting mode = new ModeSetting("Mode", "Box", "Box", "Outline");
        private final BooleanSetting players = new BooleanSetting("Players", true);
        private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
        private final BooleanSetting items = new BooleanSetting("Items", false);
        private final NumberSetting range = new NumberSetting("Range", 48, 8, 128, 1);
        public ESP() { super("ESP", "Highlights entities through walls.", Category.RENDER); add(mode); add(players); add(mobs); add(items); add(range); }

        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            if (nullCheck()) return;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (mc.player.distanceTo(e) > range.getValue()) continue;
                float r, g, b;
                if (e instanceof Player) { if (!players.getValue()) continue; r = .4f; g = .7f; b = 1f; }
                else if (e instanceof Monster) { if (!mobs.getValue()) continue; r = 1f; g = .4f; b = .4f; }
                else if (e instanceof ItemEntity) { if (!items.getValue()) continue; r = 1f; g = 1f; b = .2f; }
                else continue;
                Vec3 lerp = e.getPosition(pt);
                AABB box = e.getBoundingBox().move(lerp.subtract(e.position())).inflate(0.05);
                RenderUtils.lineBox(pose, vc, box, r, g, b, 1f);
                if (mode.is("Box")) RenderUtils.filledBox(pose, box, r, g, b, 0.15f);
            }
        }
    }

    public static class Tracers extends Module {
        private final NumberSetting range = new NumberSetting("Range", 64, 8, 200, 1);
        public Tracers() { super("Tracers", "Lines to players.", Category.RENDER); add(range); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            if (nullCheck()) return;
            Vec3 eye = mc.player.getPosition(pt).add(0, mc.player.getEyeHeight(), 0);
            var lp = pose.last();
            for (Player p : mc.level.players()) {
                if (p == mc.player || mc.player.distanceTo(p) > range.getValue()) continue;
                Vec3 end = p.getPosition(pt).add(0, p.getBbHeight() / 2.0, 0);
                vc.addVertex(lp, (float) eye.x, (float) eye.y, (float) eye.z).setColor(.4f, .7f, 1f, 1f).setNormal(lp, 0, 1, 0);
                vc.addVertex(lp, (float) end.x, (float) end.y, (float) end.z).setColor(.4f, .7f, 1f, 1f).setNormal(lp, 0, 1, 0);
            }
        }
    }

    public static class Nametags extends Module {
        public Nametags() { super("Nametags", "Always-visible nametags.", Category.RENDER); }
    }

    public static class StorageESP extends Module {
        public StorageESP() { super("StorageESP", "Highlights containers.", Category.RENDER); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            if (nullCheck()) return;
            for (BlockEntity be : mc.level.blockEntityList) {
                if (!(be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity
                        || be instanceof ShulkerBoxBlockEntity || be instanceof FurnaceBlockEntity
                        || be instanceof HopperBlockEntity)) continue;
                RenderUtils.lineBox(pose, vc, new AABB(be.getBlockPos()).inflate(0.02), 1f, .85f, .2f, 1f);
            }
        }
    }

    public static class HoleESP extends Module {
        private final NumberSetting range = new NumberSetting("Range", 16, 4, 32, 1);
        public HoleESP() { super("HoleESP", "Highlights safe holes.", Category.RENDER); add(range); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            if (nullCheck()) return;
            BlockPos p = mc.player.blockPosition();
            int r = range.getInt();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-r, -r, -r), p.offset(r, r, r))) {
                if (!mc.level.isEmptyBlock(pos) || !mc.level.isEmptyBlock(pos.above())) continue;
                if (mc.level.isEmptyBlock(pos.below())) continue;
                if (mc.level.isEmptyBlock(pos.north()) || mc.level.isEmptyBlock(pos.south())
                        || mc.level.isEmptyBlock(pos.east()) || mc.level.isEmptyBlock(pos.west())) continue;
                RenderUtils.filledBox(pose, new AABB(pos), .3f, 1f, .5f, .4f);
            }
        }
    }

    public static class BlockESP extends Module {
        private final StringListSetting blocks = new StringListSetting("Blocks", "diamond_ore", "deepslate_diamond_ore", "ancient_debris");
        private final NumberSetting range = new NumberSetting("Range", 24, 4, 64, 1);
        public BlockESP() { super("BlockESP", "Highlights chosen blocks.", Category.RENDER); add(blocks); add(range); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            if (nullCheck()) return;
            BlockPos p = mc.player.blockPosition();
            int r = range.getInt();
            for (BlockPos pos : BlockPos.betweenClosed(p.offset(-r, -r, -r), p.offset(r, r, r))) {
                var st = mc.level.getBlockState(pos);
                if (st.isAir()) continue;
                String id = BuiltInRegistries.BLOCK.getKey(st.getBlock()).getPath();
                if (blocks.getValues().contains(id)) RenderUtils.lineBox(pose, vc, new AABB(pos), .2f, 1f, 1f, 1f);
            }
        }
    }

    public static class FullBright extends Module {
        private double old = 1.0;
        public FullBright() { super("FullBright", "See in the dark.", Category.RENDER); }
        @Override public void onEnable() { if (mc.options != null) { old = mc.options.gamma().get(); mc.options.gamma().set(10.0); } }
        @Override public void onDisable() { if (mc.options != null) mc.options.gamma().set(old); }
    }

    public static class XRay extends Module {
        public XRay() { super("XRay", "Ore X-ray.", Category.RENDER); }
        public boolean visible(net.minecraft.world.level.block.state.BlockState s) {
            return s.isAir() || !s.canOcclude() || s.is(Blocks.DIAMOND_ORE) || s.is(Blocks.DEEPSLATE_DIAMOND_ORE)
                    || s.is(Blocks.IRON_ORE) || s.is(Blocks.GOLD_ORE) || s.is(Blocks.ANCIENT_DEBRIS);
        }
    }

    public static class FreeCam extends Module { public FreeCam() { super("FreeCam", "Detached camera.", Category.RENDER); } }
    public static class CameraClip extends Module { public CameraClip() { super("CameraClip", "No camera collision.", Category.RENDER); } }

    public static class NoRender extends Module {
        private final BooleanSetting fire = new BooleanSetting("Fire", true);
        private final BooleanSetting weather = new BooleanSetting("Weather", true);
        public NoRender() { super("NoRender", "Hide annoying visuals.", Category.RENDER); add(fire); add(weather); }
    }

    public static class Trajectories extends Module { public Trajectories() { super("Trajectories", "Projectile prediction.", Category.RENDER); } }

    public static class Zoom extends Module {
        private final NumberSetting factor = new NumberSetting("Factor", 4, 2, 10, 1);
        public Zoom() { super("Zoom", "Zoom key.", Category.RENDER); add(factor); }
    }

    public static class Breadcrumbs extends Module {
        private final java.util.List<Vec3> pts = new java.util.ArrayList<>();
        public Breadcrumbs() { super("Breadcrumbs", "Trail behind you.", Category.RENDER); }
        @Override public void onTick() {
            if (nullCheck()) return;
            pts.add(mc.player.position());
            if (pts.size() > 500) pts.remove(0);
        }
        @Override public void onDisable() { pts.clear(); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            var lp = pose.last();
            for (int i = 1; i < pts.size(); i++) {
                Vec3 a = pts.get(i - 1), b = pts.get(i);
                vc.addVertex(lp, (float) a.x, (float) a.y, (float) a.z).setColor(.5f, 1f, .8f, 1f).setNormal(lp, 0, 1, 0);
                vc.addVertex(lp, (float) b.x, (float) b.y, (float) b.z).setColor(.5f, 1f, .8f, 1f).setNormal(lp, 0, 1, 0);
            }
        }
    }

    public static class Waypoints extends Module {
        public static final java.util.Map<String, Vec3> POINTS = new java.util.HashMap<>();
        public Waypoints() { super("Waypoints", "Marks saved coords.", Category.RENDER); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            for (Vec3 v : POINTS.values())
                RenderUtils.filledBox(pose, new AABB(v.x - .2, v.y, v.z - .2, v.x + .2, v.y + 2, v.z + .2), 1f, .3f, .9f, .5f);
        }
    }

    public static class LogoutSpots extends Module {
        public static final java.util.Map<String, Vec3> SPOTS = new java.util.HashMap<>();
        public LogoutSpots() { super("LogoutSpots", "Marks logout spots.", Category.RENDER); }
        @Override public void onRender3D(PoseStack pose, VertexConsumer vc, float pt) {
            for (Vec3 v : SPOTS.values())
                RenderUtils.lineBox(pose, vc, new AABB(v.x - .5, v.y, v.z - .5, v.x + .5, v.y + 2, v.z + .5), 1f, 1f, 0f, 1f);
        }
    }

    public static class NewChunks extends Module { public NewChunks() { super("NewChunks", "Highlights new chunks.", Category.RENDER); } }

    public static class Search extends Module {
        private final StringListSetting blocks = new StringListSetting("Blocks", "diamond_ore", "netherite");
        public Search() { super("Search", "Alerts on searched blocks.", Category.RENDER); add(blocks); }
    }

    public static class VoidESP extends Module { public VoidESP() { super("VoidESP", "Highlights void holes.", Category.RENDER); } }
    public static class PopChams extends Module { public PopChams() { super("PopChams", "Flash on totem pops.", Category.RENDER); } }
}
