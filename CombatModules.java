package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import com.venice.venicehack.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public final class CombatModules {
    private CombatModules() {}
    private static final Random RNG = new Random();

    public static class KillAura extends Module {
        private final NumberSetting range = new NumberSetting("Range", 3.0, 2.0, 6.0, 0.1);
        private final NumberSetting cps = new NumberSetting("CPS", 12, 1, 20, 1);
        private final ModeSetting rotation = new ModeSetting("Rotation", "Smooth", "Instant", "Smooth", "None");
        private final BooleanSetting players = new BooleanSetting("Players", true);
        private final BooleanSetting mobs = new BooleanSetting("Mobs", true);
        private final BooleanSetting randomizeReach = new BooleanSetting("RandomizeReach", true);
        private final BooleanSetting ignoreFriends = new BooleanSetting("IgnoreFriends", true);
        private final Timer timer = new Timer();

        public KillAura() {
            super("KillAura", "Attacks nearby entities automatically.", Category.COMBAT);
            add(range); add(cps); add(rotation); add(players); add(mobs); add(randomizeReach); add(ignoreFriends);
        }

        @Override public void onTick() {
            if (nullCheck()) return;
            double reach = range.getValue();
            if (randomizeReach.getValue()) reach += (RNG.nextDouble() - 0.5) * 0.4;
            LivingEntity target = null;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (!(e instanceof LivingEntity le) || e == mc.player || le.isDeadOrDying()) continue;
                boolean isPlayer = le instanceof Player;
                if (!players.getValue() && isPlayer) continue;
                if (!mobs.getValue() && !isPlayer) continue;
                if (ignoreFriends.getValue() && isPlayer
                        && com.venice.venicehack.VeniceHack.friends.isFriend(le.getName().getString())) continue;
                if (mc.player.distanceTo(e) > reach) continue;
                target = le; break;
            }
            if (target == null) return;
            if (rotation.is("Instant")) RotationUtils.faceEntity(target, true);
            else if (rotation.is("Smooth")) RotationUtils.faceEntity(target, false);
            if (timer.passedAndReset(1000.0 / cps.getValue())) {
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    public static class AutoCrystal extends Module {
        private final NumberSetting placeRange = new NumberSetting("PlaceRange", 4.5, 2, 6, 0.1);
        private final NumberSetting breakRange = new NumberSetting("BreakRange", 4.5, 2, 6, 0.1);
        private final Timer timer = new Timer();
        public AutoCrystal() { super("AutoCrystal", "Places and detonates end crystals.", Category.COMBAT); add(placeRange); add(breakRange); }

        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof EndCrystal ce && mc.player.distanceTo(ce) <= breakRange.getValue()) {
                    if (timer.passedAndReset(50)) { mc.gameMode.attack(mc.player, ce); mc.player.swing(InteractionHand.MAIN_HAND); }
                    return;
                }
            }
            BlockPos best = null;
            for (BlockPos pos : BlockPos.betweenClosed(
                    mc.player.blockPosition().offset(-4, -2, -4), mc.player.blockPosition().offset(4, 2, 4))) {
                if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > placeRange.getValue() * placeRange.getValue()) continue;
                var st = mc.level.getBlockState(pos);
                if (!st.is(Blocks.OBSIDIAN) && !st.is(Blocks.BEDROCK)) continue;
                if (!mc.level.isEmptyBlock(pos.above())) continue;
                best = pos.immutable(); break;
            }
            if (best == null) return;
            int slot = PlayerUtils.findItem(Items.END_CRYSTAL);
            if (slot == -1) return;
            if (slot < 9) mc.player.getInventory().selected = slot;
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(best).add(0, 0.5, 0), Direction.UP, best, false));
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static class AutoTotem extends Module {
        private final NumberSetting delay = new NumberSetting("Delay", 50, 0, 500, 10);
        private final Timer timer = new Timer();
        public AutoTotem() { super("AutoTotem", "Keeps a totem in your offhand.", Category.COMBAT); add(delay); }

        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
            if (!timer.passedAndReset(delay.getValue())) return;
            int slot = PlayerUtils.findItem(Items.TOTEM_OF_UNDYING);
            if (slot == -1) return;
            mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId,
                    PlayerUtils.invToMenuSlot(slot), 40, ClickType.SWAP, mc.player);
        }
    }

    public static class AutoArmor extends Module {
        private final Timer timer = new Timer();
        public AutoArmor() { super("AutoArmor", "Equips the best armour.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(100)) return;
            for (int i = 0; i < 36; i++) {
                var st = mc.player.getInventory().getItem(i);
                if (st.isEmpty() || !(st.getItem() instanceof ArmorItem ai)) continue;
                int armorSlot = 8 - ai.getEquipmentSlot().getIndex();
                var cur = mc.player.getInventory().getItem(36 + (7 - armorSlot));
                if (!cur.isEmpty() && cur.getItem() instanceof ArmorItem cai && cai.getDefense() >= ai.getDefense()) continue;
                mc.gameMode.handleInventoryMouseClick(mc.player.inventoryMenu.containerId,
                        PlayerUtils.invToMenuSlot(i), 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    public static class AutoPot extends Module {
        private final NumberSetting health = new NumberSetting("Health", 15, 1, 20, 1);
        private final Timer timer = new Timer();
        public AutoPot() { super("AutoPot", "Throws a healing potion when low.", Category.COMBAT); add(health); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mc.player.getHealth() + mc.player.getAbsorptionAmount() > health.getValue()) return;
            if (!timer.passedAndReset(500)) return;
            for (int i = 0; i < 9; i++) {
                var s = mc.player.getInventory().getItem(i);
                if (s.is(Items.SPLASH_POTION) || s.is(Items.LINGERING_POTION)) {
                    mc.player.getInventory().selected = i;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    return;
                }
            }
        }
    }

    public static class AutoBlock extends Module {
        private final ModeSetting mode = new ModeSetting("Mode", "Packet", "Packet", "Hold");
        public AutoBlock() { super("AutoBlock", "Auto-blocks with your shield.", Category.COMBAT); add(mode); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mode.is("Hold")) mc.options.keyUse.setDown(true);
            else if (mc.player.getOffhandItem().is(Items.SHIELD) && !mc.player.isUsingItem())
                mc.gameMode.useItem(mc.player, InteractionHand.OFF_HAND);
        }
    }

    public static class AutoWeb extends Module {
        private final NumberSetting range = new NumberSetting("Range", 4, 2, 6, 1);
        private final Timer timer = new Timer();
        public AutoWeb() { super("AutoWeb", "Webs players you fight.", Category.COMBAT); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(100)) return;
            var t = PlayerUtils.targets(range.getValue(), true);
            if (t.isEmpty()) return;
            int slot = PlayerUtils.findItem(Items.COBWEB);
            if (slot == -1) return;
            if (slot < 9) mc.player.getInventory().selected = slot;
            BlockPos p = t.get(0).blockPosition();
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(p), Direction.UP, p.below(), false));
        }
    }

    public static class HoleFiller extends Module {
        private final NumberSetting range = new NumberSetting("Range", 3, 1, 5, 1);
        private final Timer timer = new Timer();
        public HoleFiller() { super("HoleFiller", "Fills holes with obsidian.", Category.COMBAT); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(150)) return;
            int slot = PlayerUtils.findItem(Items.OBSIDIAN);
            if (slot == -1) return;
            if (slot < 9) mc.player.getInventory().selected = slot;
            int r = range.getInt();
            for (BlockPos pos : BlockPos.betweenClosed(
                    mc.player.blockPosition().offset(-r, -r, -r), mc.player.blockPosition().offset(r, r, r))) {
                if (!mc.level.isEmptyBlock(pos) || !mc.level.isEmptyBlock(pos.above())) continue;
                if (mc.level.isEmptyBlock(pos.below())) continue;
                if (mc.level.isEmptyBlock(pos.north()) || mc.level.isEmptyBlock(pos.south())
                        || mc.level.isEmptyBlock(pos.east()) || mc.level.isEmptyBlock(pos.west())) continue;
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos.below(), false));
                return;
            }
        }
    }

    public static class AutoCity extends Module {
        private final NumberSetting range = new NumberSetting("Range", 4, 2, 6, 1);
        private final Timer timer = new Timer();
        public AutoCity() { super("AutoCity", "Breaks blocks around a target.", Category.COMBAT); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(60)) return;
            var t = PlayerUtils.targets(range.getValue(), true);
            if (t.isEmpty()) return;
            BlockPos p = t.get(0).blockPosition();
            for (BlockPos o : new BlockPos[]{p.north(), p.south(), p.east(), p.west()}) {
                if (mc.level.isEmptyBlock(o)) continue;
                mc.gameMode.continueDestroyBlock(o, Direction.UP);
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    public static class BowAimbot extends Module {
        public BowAimbot() { super("BowAimbot", "Aims your bow at the nearest target.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck() || !mc.player.isUsingItem()) return;
            var t = PlayerUtils.targets(32, true);
            if (!t.isEmpty()) RotationUtils.faceEntity(t.get(0), false);
        }
    }

    public static class AntiCrystal extends Module {
        public AntiCrystal() { super("AntiCrystal", "Breaks crystals before they pop.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            for (Entity e : mc.level.entitiesForRendering())
                if (e instanceof EndCrystal ce && mc.player.distanceTo(ce) <= 4.0) {
                    mc.gameMode.attack(mc.player, ce);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    return;
                }
        }
    }

    public static class Reach extends Module {
        private final NumberSetting reach = new NumberSetting("Reach", 4.5, 3.0, 6.0, 0.1);
        public Reach() { super("Reach", "Extends your reach.", Category.COMBAT); add(reach); }
    }

    public static class Velocity extends Module {
        public static boolean active;
        private final NumberSetting horizontal = new NumberSetting("Horizontal", 0, 0, 100, 1);
        private final NumberSetting vertical = new NumberSetting("Vertical", 0, 0, 100, 1);
        public Velocity() { super("Velocity", "Reduces knockback.", Category.COMBAT); add(horizontal); add(vertical); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
        public double h() { return horizontal.getValue() / 100.0; }
        public double v() { return vertical.getValue() / 100.0; }
    }

    public static class AutoClicker extends Module {
        private final NumberSetting cps = new NumberSetting("CPS", 10, 1, 25, 1);
        private final BooleanSetting randomize = new BooleanSetting("Randomize", true);
        private final Timer timer = new Timer();
        public AutoClicker() { super("AutoClicker", "Clicks while holding attack.", Category.COMBAT); add(cps); add(randomize); }
        @Override public void onTick() {
            if (nullCheck() || !mc.options.keyAttack.isDown()) return;
            double c = cps.getValue();
            if (randomize.getValue()) c += (RNG.nextDouble() - 0.5) * 4;
            if (!timer.passedAndReset(1000.0 / c)) return;
            if (mc.hitResult instanceof EntityHitResult ehr && ehr.getEntity() instanceof LivingEntity le) {
                mc.gameMode.attack(mc.player, le);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }
    }

    public static class Surround extends Module {
        private final Timer timer = new Timer();
        public Surround() { super("Surround", "Obsidian around your feet.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(100)) return;
            int slot = PlayerUtils.findItem(Items.OBSIDIAN);
            if (slot == -1) return;
            if (slot < 9) mc.player.getInventory().selected = slot;
            BlockPos p = mc.player.blockPosition();
            for (BlockPos o : new BlockPos[]{p.north(), p.south(), p.east(), p.west()}) {
                if (!mc.level.isEmptyBlock(o)) continue;
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(o), Direction.UP, o.below(), false));
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    public static class AutoTrap extends Module {
        private final Timer timer = new Timer();
        public AutoTrap() { super("AutoTrap", "Traps players in obsidian.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(150)) return;
            var t = PlayerUtils.targets(4, true);
            if (t.isEmpty()) return;
            int slot = PlayerUtils.findItem(Items.OBSIDIAN);
            if (slot == -1) return;
            if (slot < 9) mc.player.getInventory().selected = slot;
            BlockPos p = t.get(0).blockPosition();
            for (BlockPos o : new BlockPos[]{p.north(), p.south(), p.east(), p.west(), p.above(2)}) {
                if (!mc.level.isEmptyBlock(o)) continue;
                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                        new BlockHitResult(Vec3.atCenterOf(o), Direction.UP, o.below(), false));
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    public static class TriggerBot extends Module {
        private final NumberSetting delay = new NumberSetting("Delay", 0, 0, 500, 10);
        private final Timer timer = new Timer();
        public TriggerBot() { super("TriggerBot", "Attacks whatever you look at.", Category.COMBAT); add(delay); }
        @Override public void onTick() {
            if (nullCheck() || !(mc.hitResult instanceof EntityHitResult ehr)) return;
            if (!(ehr.getEntity() instanceof LivingEntity)) return;
            if (!timer.passedAndReset(delay.getValue())) return;
            mc.gameMode.attack(mc.player, ehr.getEntity());
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static class AimAssist extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 35, 1, 90, 1);
        public AimAssist() { super("AimAssist", "Smooth aim onto targets.", Category.COMBAT); add(speed); }
        @Override public void onTick() {
            if (nullCheck()) return;
            var t = PlayerUtils.targets(6, true);
            if (t.isEmpty()) return;
            float[] r = RotationUtils.lookAt(t.get(0).position().add(0, t.get(0).getBbHeight() / 2.0, 0));
            float s = speed.getFloat() * 0.5f;
            float dy = net.minecraft.util.Mth.wrapDegrees(r[0] - mc.player.getYRot());
            float dp = net.minecraft.util.Mth.wrapDegrees(r[1] - mc.player.getXRot());
            mc.player.setYRot(mc.player.getYRot() + Math.copySign(Math.min(Math.abs(dy), s), dy));
            mc.player.setXRot(mc.player.getXRot() + Math.copySign(Math.min(Math.abs(dp), s), dp));
        }
    }

    public static class Criticals extends Module {
        public Criticals() { super("Criticals", "Spoofs a jump before hitting.", Category.COMBAT); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (mc.options.keyAttack.isDown() && mc.player.onGround() && !mc.player.isInWater())
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.11, mc.player.getDeltaMovement().z);
        }
    }

    public static class AntiReach extends Module {
        private final BooleanSetting alert = new BooleanSetting("Alert", true);
        public AntiReach() { super("AntiReach", "Warns when someone reaches you.", Category.COMBAT); add(alert); }
        @Override public void onTick() {
            if (nullCheck() || !alert.getValue()) return;
            for (Player p : mc.level.players()) {
                if (p == mc.player) continue;
                double d = mc.player.distanceTo(p);
                if (d > 4.0 && d < 8.0 && p.swinging)
                    ChatUtils.send(p.getName().getString() + " reach " + String.format("%.2f", d));
            }
        }
    }

    public static class ReachDisplay extends Module {
        public ReachDisplay() { super("ReachDisplay", "Shows your last hit distance.", Category.COMBAT); }
    }
}
