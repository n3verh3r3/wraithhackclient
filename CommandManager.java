package com.venice.venicehack.core;

import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.util.ChatUtils;

public class CommandManager {
    private String prefix = ".";

    public boolean handle(String raw) {
        if (!raw.startsWith(prefix)) return false;
        String[] a = raw.substring(prefix.length()).trim().split(" ");
        if (a.length == 0 || a[0].isEmpty()) return true;
        switch (a[0].toLowerCase()) {
            case "help" -> ChatUtils.send(".toggle <mod> | .bind <mod> <key> | .friend add/remove <n> | .profile save/load <n> | .panic | .prefix <c>");
            case "toggle", "t" -> {
                if (a.length < 2) { ChatUtils.send("Usage: .toggle <module>"); return true; }
                Module m = VeniceHack.modules.getByName(a[1]);
                if (m == null) ChatUtils.send("Unknown module: " + a[1]);
                else { m.toggle(); ChatUtils.send(m.getName() + " -> " + (m.isEnabled() ? "ON" : "OFF")); }
            }
            case "bind" -> {
                if (a.length < 3) { ChatUtils.send("Usage: .bind <module> <keycode>"); return true; }
                Module m = VeniceHack.modules.getByName(a[1]);
                if (m != null) { m.setKeybind(Integer.parseInt(a[2])); ChatUtils.send("Bound " + m.getName()); }
            }
            case "friend" -> {
                if (a.length < 3) { ChatUtils.send("Usage: .friend add/remove <name>"); return true; }
                if (a[1].equalsIgnoreCase("add")) { VeniceHack.friends.add(a[2]); ChatUtils.send("Added friend " + a[2]); }
                else { VeniceHack.friends.remove(a[2]); ChatUtils.send("Removed friend " + a[2]); }
            }
            case "profile" -> {
                if (a.length < 3) { ChatUtils.send("Usage: .profile save/load <name>"); return true; }
                if (a[1].equalsIgnoreCase("save")) { VeniceHack.config.saveProfile(a[2]); ChatUtils.send("Saved profile " + a[2]); }
                else { VeniceHack.config.loadProfile(a[2]); ChatUtils.send("Loaded profile " + a[2]); }
            }
            case "panic" -> VeniceHack.modules.getModules().forEach(m -> m.setEnabled(false));
            case "prefix" -> { if (a.length > 1) prefix = a[1]; }
            default -> ChatUtils.send("Unknown command. Try .help");
        }
        return true;
    }
}
