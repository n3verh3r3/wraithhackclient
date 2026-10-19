package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import net.minecraft.world.phys.Vec3;

public final class MovementModules {
    private MovementModules() {}

    public static class Fly extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
        public Fly() { super("Fly", "Survival flight.", Category.MOVEMENT); add(speed); }
        @Override public void onTick() {
            if (nullCheck()) return;
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlyingSpeed(speed.getFloat() * 0.05f);
        }
        @Override public void onDisable() { if (mc.player != null) mc.player.getAbilities().flying = false; }
    }

    public static class Speed extends Module {
        private final NumberSetting multiplier = new NumberSetting("Multiplier", 1.4, 1.0, 4.0, 0.05);
        public Speed() { super("Speed", "Faster ground movement.", Category.MOVEMENT); add(multiplier); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.onGround() || mc.player.isInWater()) return;
            Vec3 v = mc.player.getDeltaMovement();
            if (Math.hypot(v.x, v.z) < 0.01) return;
            mc.player.setDeltaMovement(v.x * multiplier.getValue(), v.y, v.z * multiplier.getValue());
        }
    }

    public static class Sprint extends Module {
        public Sprint() { super("Sprint", "Always sprinting.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.zza > 0 && !mc.player.isShiftKeyDown() && !mc.player.isUsingItem()) mc.player.setSprinting(true);
        }
    }

    public static class Step extends Module {
        private final NumberSetting height = new NumberSetting("Height", 1.0, 0.5, 2.5, 0.1);
        public Step() { super("Step", "Instant step-up.", Category.MOVEMENT); add(height); }
        @Override public void onTick() { if (!nullCheck()) mc.player.setMaxUpStep(height.getFloat()); }
        @Override public void onDisable() { if (mc.player != null) mc.player.setMaxUpStep(0.6f); }
    }

    public static class NoFall extends Module {
        public static boolean active;
        public NoFall() { super("NoFall", "No fall damage.", Category.MOVEMENT); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
        @Override public void onTick() { if (!nullCheck() && mc.player.fallDistance > 2) mc.player.fallDistance = 0; }
    }

    public static class Jesus extends Module {
        public Jesus() { super("Jesus", "Walk on water.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.isInWater() && mc.player.getDeltaMovement().y < 0.1)
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.08, mc.player.getDeltaMovement().z);
        }
    }

    public static class Spider extends Module {
        public Spider() { super("Spider", "Climb walls.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.horizontalCollision && mc.player.zza > 0)
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.2, mc.player.getDeltaMovement().z);
        }
    }

    public static class LongJump extends Module {
        private final NumberSetting boost = new NumberSetting("Boost", 2.0, 1.0, 5.0, 0.1);
        public LongJump() { super("LongJump", "Big jump boost.", Category.MOVEMENT); add(boost); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.options.keyJump.isDown() && mc.player.onGround()) {
                double yaw = Math.toRadians(mc.player.getYRot());
                mc.player.setDeltaMovement(-Math.sin(yaw) * boost.getValue(), 0.42, Math.cos(yaw) * boost.getValue());
            }
        }
    }

    public static class ElytraFly extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.5, 5.0, 0.1);
        public ElytraFly() { super("ElytraFly", "Faster elytra flight.", Category.MOVEMENT); add(speed); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.isFallFlying()) return;
            mc.player.setDeltaMovement(mc.player.getViewVector(1f).scale(speed.getValue()));
        }
    }

    public static class BoatFly extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 0.5, 0.1, 3.0, 0.05);
        public BoatFly() { super("BoatFly", "Fly while riding.", Category.MOVEMENT); add(speed); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.isPassenger()) return;
            var v = mc.player.getVehicle();
            if (v == null) return;
            v.setDeltaMovement(0, mc.options.keyJump.isDown() ? speed.getValue()
                    : mc.options.keyShift.isDown() ? -speed.getValue() : 0, 0);
        }
    }

    public static class AntiVoid extends Module {
        public AntiVoid() { super("AntiVoid", "Saves you from the void.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.getY() < -10 && mc.player.getDeltaMovement().y < 0) {
                mc.player.setPos(mc.player.getX(), 100, mc.player.getZ());
                mc.player.setDeltaMovement(0, 0, 0);
            }
        }
    }

    public static class NoSlow extends Module {
        public static boolean active;
        public NoSlow() { super("NoSlow", "No slowdown using items.", Category.MOVEMENT); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static class SafeWalk extends Module {
        public SafeWalk() { super("SafeWalk", "Stop at block edges.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.onGround()) return;
            if (mc.level.isEmptyBlock(mc.player.blockPosition().below()))
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x * 0.5, mc.player.getDeltaMovement().y,
                        mc.player.getDeltaMovement().z * 0.5);
        }
    }

    public static class Parkour extends Module {
        public Parkour() { super("Parkour", "Auto-jump at edges.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.onGround()) return;
            if (mc.player.zza > 0 && mc.level.isEmptyBlock(mc.player.blockPosition().below(2))) mc.player.jumpFromGround();
        }
    }

    public static class AutoWalk extends Module {
        public AutoWalk() { super("AutoWalk", "Walks forward.", Category.MOVEMENT); }
        @Override public void onTick() { if (!nullCheck()) mc.options.keyUp.setDown(true); }
        @Override public void onDisable() { mc.options.keyUp.setDown(false); }
    }

    public static class NoHunger extends Module {
        public NoHunger() { super("NoHunger", "Sprint without hunger loss.", Category.MOVEMENT); }
    }

    public static class ReverseStep extends Module {
        public ReverseStep() { super("ReverseStep", "Instantly drop down blocks.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.onGround() && mc.player.getDeltaMovement().y <= 0 && !mc.player.isShiftKeyDown())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -0.5, mc.player.getDeltaMovement().z);
        }
    }

    public static class FastLadder extends Module {
        public FastLadder() { super("FastLadder", "Climb ladders fast.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.player.onClimbable() && mc.player.zza > 0)
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.25, mc.player.getDeltaMovement().z);
        }
    }

    public static class NoWeb extends Module {
        public NoWeb() { super("NoWeb", "No cobweb slowdown.", Category.MOVEMENT); }
    }

    public static class FastSwim extends Module {
        public FastSwim() { super("FastSwim", "Swim faster.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.isSwimming()) return;
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().scale(1.15));
        }
    }

    public static class SprintJump extends Module {
        public SprintJump() { super("SprintJump", "Auto-jump while sprinting.", Category.MOVEMENT); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.onGround() || !mc.player.isSprinting()) return;
            if (mc.player.zza > 0) mc.player.jumpFromGround();
        }
    }

    public static class Glide extends Module {
        private final NumberSetting fallSpeed = new NumberSetting("FallSpeed", 0.12, 0.02, 1.0, 0.01);
        public Glide() { super("Glide", "Slow fall.", Category.MOVEMENT); add(fallSpeed); }
        @Override public void onTick() {
            if (nullCheck() || mc.player.onGround()) return;
            if (mc.player.getDeltaMovement().y < -fallSpeed.getValue())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -fallSpeed.getValue(), mc.player.getDeltaMovement().z);
        }
    }
}
