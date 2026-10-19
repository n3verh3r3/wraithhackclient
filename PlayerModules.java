package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import com.venice.venicehack.util.PlayerUtils;
import com.venice.venicehack.util.Timer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class PlayerModules {
    private PlayerModules() {}

    public static class FastPlace extends Module {
        public FastPlace() { super("FastPlace", "No place delay.", Category.PLAYER); }
        @Override public void onTick() { if (!nullCheck()) mc.rightClickDelay = 0; }
    }

    public static class FastBreak extends Module {
        public FastBreak() { super("FastBreak", "Faster mining.", Category.PLAYER); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mc.hitResult instanceof BlockHitResult b && !mc.level.isEmptyBlock(b.getBlockPos()))
                mc.gameMode.continueDestroyBlock(b.getBlockPos(), b.getDirection());
        }
    }

    public static class AutoTool extends Module {
        public AutoTool() { super("AutoTool", "Best tool auto-switch.", Category.PLAYER); }
        @Override public void onTick() {
            if (nullCheck() || !(mc.hitResult instanceof BlockHitResult bhr) || !mc.options.keyAttack.isDown()) return;
            var st = mc.level.getBlockState(bhr.getBlockPos());
            if (st.isAir()) return;
            int best = -1; float bestSpeed = 1f;
            for (int i = 0; i < 9; i++) {
                var s = mc.player.getInventory().getItem(i);
                if (s.isEmpty()) continue;
                float sp = s.getDestroySpeed(st);
                if (sp > bestSpeed) { bestSpeed = sp; best = i; }
            }
            if (best != -1) mc.player.getInventory().selected = best;
        }
    }

    public static class Scaffold extends Module {
        public Scaffold() { super("Scaffold", "Places blocks under you.", Category.PLAYER); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            BlockPos pos = mc.player.blockPosition().below();
            if (!mc.level.isEmptyBlock(pos)) return;
            int slot = -1;
            for (int i = 0; i < 9; i++) if (mc.player.getInventory().getItem(i).getItem() instanceof BlockItem) { slot = i; break; }
            if (slot == -1) return;
            mc.player.getInventory().selected = slot;
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos.below(), false));
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    public static class ChestStealer extends Module {
        private final NumberSetting delay = new NumberSetting("Delay", 80, 0, 500, 10);
        private final Timer timer = new Timer();
        public ChestStealer() { super("ChestStealer", "Empties chests.", Category.PLAYER); add(delay); }
        @Override public void onTick() {
            if (nullCheck() || !(mc.screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> cs)) return;
            if (!timer.passedAndReset(delay.getValue())) return;
            var handler = cs.getMenu();
            for (int i = 0; i < handler.slots.size() - 36; i++) {
                if (handler.slots.get(i).getItem().isEmpty()) continue;
                mc.gameMode.handleInventoryMouseClick(handler.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                return;
            }
        }
    }

    public static class AutoEat extends Module {
        private final NumberSetting hunger = new NumberSetting("Hunger", 16, 1, 20, 1);
        public AutoEat() { super("AutoEat", "Eats when hungry.", Category.PLAYER); add(hunger); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mc.player.getFoodData().getFoodLevel() > hunger.getValue() || mc.player.isUsingItem()) return;
            for (int i = 0; i < 9; i++) {
                if (mc.player.getInventory().getItem(i).getItem().isEdible()) {
                    mc.player.getInventory().selected = i;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    return;
                }
            }
        }
    }

    public static class AutoFish extends Module { public AutoFish() { super("AutoFish", "Reels in fish.", Category.PLAYER); } }

    public static class Nuker extends Module {
        private final NumberSetting range = new NumberSetting("Range", 4, 1, 6, 1);
        private final Timer timer = new Timer();
        public Nuker() { super("Nuker", "Breaks blocks around you.", Category.PLAYER); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null || !timer.passedAndReset(60)) return;
            int r = range.getInt();
            for (BlockPos pos : BlockPos.betweenClosed(
                    mc.player.blockPosition().offset(-r, -r, -r), mc.player.blockPosition().offset(r, r, r))) {
                if (mc.level.isEmptyBlock(pos)) continue;
                mc.gameMode.continueDestroyBlock(pos, Direction.UP);
                mc.player.swing(InteractionHand.MAIN_HAND);
                return;
            }
        }
    }

    public static class InstantMine extends Module { public InstantMine() { super("InstantMine", "Near-instant mining.", Category.PLAYER); } }
    public static class FastUse extends Module { public FastUse() { super("FastUse", "Faster item use.", Category.PLAYER); } }
    public static class AutoReplenish extends Module { public AutoReplenish() { super("AutoReplenish", "Refills your hotbar.", Category.PLAYER); } }
    public static class AutoSwitch extends Module { public AutoSwitch() { super("AutoSwitch", "Swaps to the right item.", Category.PLAYER); } }
    public static class AutoFarm extends Module { public AutoFarm() { super("AutoFarm", "Harvests and replants.", Category.PLAYER); } }
    public static class AutoMine extends Module { public AutoMine() { super("AutoMine", "Auto-mines what you look at.", Category.PLAYER); } }

    public static class AutoRespawn extends Module {
        public AutoRespawn() { super("AutoRespawn", "Respawns instantly.", Category.PLAYER); }
        @Override public void onTick() {
            if (mc.player != null && mc.screen instanceof net.minecraft.client.gui.screens.DeathScreen) mc.player.respawn();
        }
    }

    public static class FastXP extends Module { public FastXP() { super("FastXP", "Throws XP fast.", Category.PLAYER); } }

    public static class InventoryCleaner extends Module {
        private final StringListSetting junk = new StringListSetting("Junk", "rotten_flesh", "poisonous_potato");
        public InventoryCleaner() { super("InventoryCleaner", "Drops junk items.", Category.PLAYER); add(junk); }
    }

    public static class AutoShearer extends Module {
        private final NumberSetting range = new NumberSetting("Range", 4, 1, 6, 1);
        public AutoShearer() { super("AutoShearer", "Shears nearby sheep.", Category.PLAYER); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            for (var e : mc.level.entitiesForRendering())
                if (e instanceof Sheep s && !s.isSheared() && mc.player.distanceTo(s) <= range.getValue()) {
                    mc.gameMode.interact(mc.player, s, InteractionHand.MAIN_HAND);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    return;
                }
        }
    }

    public static class AutoTiller extends Module { public AutoTiller() { super("AutoTiller", "Hoes ground around you.", Category.PLAYER); } }

    public static class AutoBreed extends Module {
        private final NumberSetting range = new NumberSetting("Range", 5, 1, 8, 1);
        public AutoBreed() { super("AutoBreed", "Breeds nearby animals.", Category.PLAYER); add(range); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            for (var e : mc.level.entitiesForRendering())
                if (e instanceof Animal a && !a.isBaby() && mc.player.distanceTo(a) <= range.getValue()) {
                    mc.gameMode.interact(mc.player, a, InteractionHand.MAIN_HAND);
                    return;
                }
        }
    }
}
