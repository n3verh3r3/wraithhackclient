package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import com.venice.venicehack.ui.StaffOverlay;
import com.venice.venicehack.util.ChatUtils;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

import java.util.*;

public final class AntiBanModules {
    private AntiBanModules() {}
    public static String lastChat = "";

    public static class StaffDetector extends Module {
        private final ModeSetting detection = new ModeSetting("Detection", "Smart", "Smart", "Keywords", "Team", "ColorName");
        private final StringListSetting keywords = new StringListSetting("Keywords",
                "owner", "admin", "mod", "staff", "helper", "dev", "manager", "op", "builder");
        private final BooleanSetting alertChat = new BooleanSetting("AlertChat", true);
        private final BooleanSetting alertSound = new BooleanSetting("AlertSound", true);
        private final BooleanSetting watchingAlert = new BooleanSetting("WatchingAlert", true);

        public StaffDetector() {
            super("StaffDetector", "Detects staff, shows head, alerts when watching you.", Category.ANTIBAN);
            add(detection); add(keywords); add(alertChat); add(alertSound); add(watchingAlert);
        }

        @Override public void onTick() {
            if (nullCheck() || mc.getConnection() == null) return;
            Set<UUID> now = new HashSet<>();
            for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {
                if (info.getProfile().getId().equals(mc.player.getUUID())) continue;
                if (!isStaff(info)) continue;
                now.add(info.getProfile().getId());
                if (!StaffOverlay.STAFF.containsKey(info.getProfile().getId())) {
                    StaffOverlay.STAFF.put(info.getProfile().getId(), info);
                    if (alertChat.getValue()) ChatUtils.send("STAFF > " + info.getProfile().getName() + " joined!");
                    if (alertSound.getValue())
                        mc.player.playSound(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1f, 1.5f);
                }
            }
            for (PlayerInfo e : new ArrayList<>(StaffOverlay.STAFF.values()))
                StaffOverlay.VANISHED.put(e.getProfile().getId(), mc.level.getPlayerByUUID(e.getProfile().getId()) == null);
            StaffOverlay.STAFF.values().removeIf(e -> !now.contains(e.getProfile().getId()));
        }

        private boolean isStaff(PlayerInfo info) {
            String name = info.getProfile().getName();
            if (detection.is("Keywords")) return match(name);
            if (detection.is("Team")) return info.getTeam() != null && match(info.getTeam().getName());
            if (detection.is("ColorName")) return info.getTabListDisplayName() != null && match(info.getTabListDisplayName().getString());
            if (match(name)) return true;
            if (info.getTeam() != null) {
                var t = info.getTeam();
                if (match(t.getName()) || match(t.getPlayerPrefix().getString())) return true;
            }
            return info.getTabListDisplayName() != null && match(info.getTabListDisplayName().getString());
        }

        private boolean match(String s) {
            if (s == null) return false;
            String low = s.toLowerCase().replace("§", "").replaceAll("[^a-z0-9 ]", " ").trim();
            for (String k : keywords.getValues()) if (low.contains(k.toLowerCase())) return true;
            return false;
        }
    }

    public static class AntiBan extends Module {
        private final BooleanSetting logOnStaffJoin = new BooleanSetting("LogOnStaffJoin", true);
        private final NumberSetting staffDelay = new NumberSetting("StaffDelay(ms)", 0, 0, 5000, 100);
        private final BooleanSetting logOnBanCommand = new BooleanSetting("LogOnBanCommand", true);
        private final BooleanSetting logOnLowHealth = new BooleanSetting("LogOnLowHealth", true);
        private final NumberSetting health = new NumberSetting("HealthThreshold", 6, 1, 20, 1);
        private final StringListSetting banWords = new StringListSetting("BanWords",
                "/ban", "/kick", "/mute", "/warn", "/jail", "banned", "hacking", "cheater", "clip it");
        private long staffSeenAt = -1;

        public AntiBan() {
            super("AntiBan", "Auto-log on staff / ban words / low HP.", Category.ANTIBAN);
            add(logOnStaffJoin); add(staffDelay); add(logOnBanCommand); add(logOnLowHealth); add(health); add(banWords);
        }

        @Override public void onTick() {
            if (nullCheck()) return;
            if (logOnStaffJoin.getValue() && !StaffOverlay.STAFF.isEmpty()) {
                if (staffSeenAt == -1) staffSeenAt = System.currentTimeMillis();
                if (System.currentTimeMillis() - staffSeenAt >= staffDelay.getValue()) { bail("staff"); staffSeenAt = -1; }
            }
            if (logOnBanCommand.getValue()) {
                String last = lastChat.toLowerCase();
                for (String w : banWords.getValues()) if (last.contains(w.toLowerCase())) { bail("chat:" + w); return; }
            }
            if (logOnLowHealth.getValue() && mc.player.getHealth() + mc.player.getAbsorptionAmount() < health.getValue())
                bail("lowhp");
        }

        private void bail(String why) {
            ChatUtils.send("AutoLog (" + why + ")");
            if (mc.getConnection() != null)
                mc.getConnection().getConnection().disconnect(Component.literal("VH AutoLog: " + why));
        }

        @Override public void onEnable() { StaffOverlay.STAFF.clear(); staffSeenAt = -1; }
    }

    public static class GhostMode extends Module {
        private final NumberSetting min = new NumberSetting("MinSec", 5, 1, 60, 1);
        private final NumberSetting max = new NumberSetting("MaxSec", 20, 1, 120, 1);
        private final NumberSetting chance = new NumberSetting("DisableChance%", 35, 1, 100, 1);
        private final BooleanSetting onlyVisuals = new BooleanSetting("OnlyVisuals", false);
        private final Random rng = new Random();
        private long next = 0;

        public GhostMode() { super("GhostMode", "Randomly toggles modules so you look legit.", Category.ANTIBAN); add(min); add(max); add(chance); add(onlyVisuals); }

        @Override public void onTick() {
            if (nullCheck()) return;
            long now = System.currentTimeMillis();
            if (now < next) return;
            next = now + (long) (min.getValue() * 1000 + rng.nextDouble() * (max.getValue() - min.getValue()) * 1000);
            var all = com.venice.venicehack.VeniceHack.modules.getModules();
            Module t = all.get(rng.nextInt(all.size()));
            if (t == this || (onlyVisuals.getValue() && t.getCategory() != Category.RENDER)) return;
            if (rng.nextDouble() * 100 < chance.getValue()) { if (t.isEnabled()) t.setEnabled(false); }
            else if (!t.isEnabled()) t.setEnabled(true);
        }
    }

    public static class AutoLog extends Module {
        private final KeybindSetting panicKey = new KeybindSetting("PanicKey", org.lwjgl.glfw.GLFW.GLFW_KEY_F12);
        private final BooleanSetting disableModules = new BooleanSetting("DisableModules", true);
        public AutoLog() { super("AutoLog", "Panic key: disable all + disconnect.", Category.ANTIBAN); add(panicKey); add(disableModules); }
        @Override public void onTick() {
            if (nullCheck()) return;
            if (com.mojang.blaze3d.platform.InputConstants.isKeyDown(mc.getWindow().getWindow(), panicKey.getValue())) {
                if (disableModules.getValue()) com.venice.venicehack.VeniceHack.modules.getModules().forEach(m -> m.setEnabled(false));
                if (mc.getConnection() != null) mc.getConnection().getConnection().disconnect(Component.literal("Panic!"));
            }
        }
    }

    public static class VanishDetector extends Module { public VanishDetector() { super("VanishDetector", "Detects vanished staff.", Category.ANTIBAN); } }

    public static class RotationRandomizer extends Module {
        private final NumberSetting jitter = new NumberSetting("Jitter", 3, 0, 15, 0.5);
        public RotationRandomizer() { super("RotationRandomizer", "Human aim jitter.", Category.ANTIBAN); add(jitter); }
        private final Random rng = new Random();
        @Override public void onTick() {
            if (nullCheck()) return;
            double j = jitter.getValue();
            mc.player.setYRot(mc.player.getYRot() + (float) ((rng.nextDouble() - 0.5) * j));
            mc.player.setXRot(mc.player.getXRot() + (float) ((rng.nextDouble() - 0.5) * j));
        }
    }

    public static class PingSpoof extends Module {
        public static boolean active;
        private final NumberSetting ping = new NumberSetting("Ping", 200, 0, 2000, 10);
        public PingSpoof() { super("PingSpoof", "Fakes your ping.", Category.ANTIBAN); add(ping); }
        @Override public void onEnable() { active = true; }
        @Override public void onDisable() { active = false; }
        public int get() { return ping.getInt(); }
    }

    public static class AntiCheatProfile extends Module {
        private final ModeSetting profile = new ModeSetting("Profile", "Vanilla", "Vanilla", "NCP", "Verus", "Vulcan", "Matrix", "Spartan", "Grim");
        public AntiCheatProfile() { super("AntiCheatProfile", "Tunes modules per anticheat.", Category.ANTIBAN); add(profile); }
    }

    public static class NickHider extends Module { public NickHider() { super("NickHider", "Hides your name locally.", Category.ANTIBAN); } }
}
