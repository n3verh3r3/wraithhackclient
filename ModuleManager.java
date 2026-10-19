package com.venice.venicehack.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.venice.venicehack.modules.*;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();

    public ModuleManager() {
        reg(new CombatModules.KillAura());
        reg(new CombatModules.AutoCrystal());
        reg(new CombatModules.AutoTotem());
        reg(new CombatModules.AutoArmor());
        reg(new CombatModules.AutoPot());
        reg(new CombatModules.AutoBlock());
        reg(new CombatModules.AutoWeb());
        reg(new CombatModules.HoleFiller());
        reg(new CombatModules.AutoCity());
        reg(new CombatModules.BowAimbot());
        reg(new CombatModules.AntiCrystal());
        reg(new CombatModules.Reach());
        reg(new CombatModules.Velocity());
        reg(new CombatModules.AutoClicker());
        reg(new CombatModules.Surround());
        reg(new CombatModules.AutoTrap());
        reg(new CombatModules.TriggerBot());
        reg(new CombatModules.AimAssist());
        reg(new CombatModules.Criticals());
        reg(new CombatModules.AntiReach());
        reg(new CombatModules.ReachDisplay());

        reg(new MovementModules.Fly());
        reg(new MovementModules.Speed());
        reg(new MovementModules.Sprint());
        reg(new MovementModules.Step());
        reg(new MovementModules.NoFall());
        reg(new MovementModules.Jesus());
        reg(new MovementModules.Spider());
        reg(new MovementModules.LongJump());
        reg(new MovementModules.ElytraFly());
        reg(new MovementModules.BoatFly());
        reg(new MovementModules.AntiVoid());
        reg(new MovementModules.NoSlow());
        reg(new MovementModules.SafeWalk());
        reg(new MovementModules.Parkour());
        reg(new MovementModules.AutoWalk());
        reg(new MovementModules.NoHunger());
        reg(new MovementModules.ReverseStep());
        reg(new MovementModules.FastLadder());
        reg(new MovementModules.NoWeb());
        reg(new MovementModules.FastSwim());
        reg(new MovementModules.SprintJump());
        reg(new MovementModules.Glide());

        reg(new RenderModules.ESP());
        reg(new RenderModules.Tracers());
        reg(new RenderModules.Nametags());
        reg(new RenderModules.StorageESP());
        reg(new RenderModules.HoleESP());
        reg(new RenderModules.BlockESP());
        reg(new RenderModules.FullBright());
        reg(new RenderModules.XRay());
        reg(new RenderModules.FreeCam());
        reg(new RenderModules.CameraClip());
        reg(new RenderModules.NoRender());
        reg(new RenderModules.Trajectories());
        reg(new RenderModules.Zoom());
        reg(new RenderModules.Breadcrumbs());
        reg(new RenderModules.Waypoints());
        reg(new RenderModules.LogoutSpots());
        reg(new RenderModules.NewChunks());
        reg(new RenderModules.Search());
        reg(new RenderModules.VoidESP());
        reg(new RenderModules.PopChams());

        reg(new PlayerModules.FastPlace());
        reg(new PlayerModules.FastBreak());
        reg(new PlayerModules.AutoTool());
        reg(new PlayerModules.Scaffold());
        reg(new PlayerModules.ChestStealer());
        reg(new PlayerModules.AutoEat());
        reg(new PlayerModules.AutoFish());
        reg(new PlayerModules.Nuker());
        reg(new PlayerModules.InstantMine());
        reg(new PlayerModules.FastUse());
        reg(new PlayerModules.AutoReplenish());
        reg(new PlayerModules.AutoSwitch());
        reg(new PlayerModules.AutoFarm());
        reg(new PlayerModules.AutoMine());
        reg(new PlayerModules.AutoRespawn());
        reg(new PlayerModules.FastXP());
        reg(new PlayerModules.InventoryCleaner());
        reg(new PlayerModules.AutoShearer());
        reg(new PlayerModules.AutoTiller());
        reg(new PlayerModules.AutoBreed());

        reg(new WorldModules.Timer());
        reg(new WorldModules.AutoSign());
        reg(new WorldModules.Excavator());
        reg(new WorldModules.Flattener());
        reg(new WorldModules.BuildRandom());
        reg(new WorldModules.StashFinder());
        reg(new WorldModules.TpsSync());
        reg(new WorldModules.AutoSell());
        reg(new WorldModules.AutoSmelt());
        reg(new WorldModules.ChestScaffold());

        reg(new MiscModules.FakePlayer());
        reg(new MiscModules.FakeLag());
        reg(new MiscModules.Blink());
        reg(new MiscModules.PacketFly());
        reg(new MiscModules.NoRotate());
        reg(new MiscModules.AntiKick());
        reg(new MiscModules.AutoReconnect());
        reg(new MiscModules.Announcer());
        reg(new MiscModules.Phase());
        reg(new MiscModules.Panic());
        reg(new MiscModules.MiddleClick());
        reg(new MiscModules.FakeRotation());
        reg(new MiscModules.GhostBlocks());
        reg(new MiscModules.ChatSuffix());
        reg(new MiscModules.EntitySpeed());
        reg(new MiscModules.MiddleClickPearl());
        reg(new MiscModules.PacketLogger());
        reg(new MiscModules.Friends());

        reg(new AntiBanModules.StaffDetector());
        reg(new AntiBanModules.AntiBan());
        reg(new AntiBanModules.GhostMode());
        reg(new AntiBanModules.AutoLog());
        reg(new AntiBanModules.VanishDetector());
        reg(new AntiBanModules.RotationRandomizer());
        reg(new AntiBanModules.PingSpoof());
        reg(new AntiBanModules.AntiCheatProfile());
        reg(new AntiBanModules.NickHider());
    }

    private void reg(Module m) { modules.add(m); }
    public List<Module> getModules() { return modules; }
    public List<Module> getByCategory(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : modules) if (m.getCategory() == c) out.add(m);
        return out;
    }
    public Module getByName(String n) {
        for (Module m : modules) if (m.getName().equalsIgnoreCase(n)) return m;
        return null;
    }

    public void onTick() {
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try { m.onTick(); } catch (Throwable ignored) {}
        }
    }

    public void onRender3D(WorldRenderContext ctx) {
        PoseStack pose = ctx.matrixStack();
        VertexConsumer vc = ctx.consumers().getBuffer(RenderType.lines());
        pose.pushPose();
        Vec3 cam = ctx.camera().getPosition();
        pose.translate(-cam.x, -cam.y, -cam.z);
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try { m.onRender3D(pose, vc, ctx.tickCounter().getGameTimeDeltaPartialTick(false)); }
            catch (Throwable ignored) {}
        }
        pose.popPose();
    }
}
