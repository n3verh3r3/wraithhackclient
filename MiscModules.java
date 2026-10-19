package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import com.venice.venicehack.util.PlayerUtils;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

public final class MiscModules {
    private MiscModules() {}

    public static class FakePlayer extends Module { public FakePlayer() { super("FakePlayer", "Spawns a fake player.", Category.MISC); } }

    public static class FakeLag extends Module {
        public static boolean active;
        private final NumberSetting delay = new NumberSetting("Delay", 200, 50, 2000, 50);
        public FakeLag() { super("FakeLag", "Delays your packets.", Category.MISC); add(delay); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static class Blink extends Module {
        public static boolean active;
        public Blink() { super("Blink", "Holds outgoing packets.", Category.MISC); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static class PacketFly extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
        public PacketFly() { super("PacketFly", "Packet-based flight.", Category.MISC); add(speed); }
    }

    public static class NoRotate extends Module {
        public static boolean active;
        public NoRotate() { super("NoRotate", "Ignores rotation corrections.", Category.MISC); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
    }

    public static class AntiKick extends Module {
        public AntiKick() { super("AntiKick", "Prevents AFK kicks.", Category.MISC); }
        @Override public void onTick() {
            if (nullCheck() || mc.getConnection() == null) return;
            if (mc.player.tickCount % 40 == 0)
                mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(
                        mc.player.getX(), mc.player.getY() + 0.0001, mc.player.getZ(), true));
        }
    }

    public static class AutoReconnect extends Module { public AutoReconnect() { super("AutoReconnect", "Rejoins after disconnect.", Category.MISC); } }
    public static class Announcer extends Module { public Announcer() { super("Announcer", "Announces actions in chat.", Category.MISC); } }
    public static class Phase extends Module { public Phase() { super("Phase", "Walk through blocks.", Category.MISC); } }
    public static class Panic extends Module { public Panic() { super("Panic", "Disables everything + disconnects.", Category.MISC); } }
    public static class MiddleClick extends Module { public MiddleClick() { super("MiddleClick", "Middle-click actions.", Category.MISC); } }
    public static class FakeRotation extends Module { public FakeRotation() { super("FakeRotation", "Server-side-only rotation.", Category.MISC); } }
    public static class GhostBlocks extends Module { public GhostBlocks() { super("GhostBlocks", "Client-side block removal.", Category.MISC); } }

    public static class ChatSuffix extends Module {
        private final ModeSetting suffix = new ModeSetting("Suffix", "VH", "VH", "[V]", ">");
        public ChatSuffix() { super("ChatSuffix", "Appends a suffix to chat.", Category.MISC); add(suffix); }
    }

    public static class EntitySpeed extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 1.5, 1.0, 5.0, 0.1);
        public EntitySpeed() { super("EntitySpeed", "Faster riding.", Category.MISC); add(speed); }
    }

    public static class MiddleClickPearl extends Module {
        public MiddleClickPearl() { super("MiddleClickPearl", "Middle-click throws a pearl.", Category.MISC); }
        @Override public void onTick() {
            if (nullCheck() || mc.gameMode == null) return;
            if (mc.mouseHandler.isMiddlePressed()) {
                int slot = PlayerUtils.findItem(Items.ENDER_PEARL);
                if (slot == -1) return;
                if (slot < 9) mc.player.getInventory().selected = slot;
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            }
        }
    }

    public static class PacketLogger extends Module { public PacketLogger() { super("PacketLogger", "Logs packets in chat.", Category.MISC); } }
    public static class Friends extends Module { public Friends() { super("Friends", "Marks friends in the world.", Category.MISC); } }
}
