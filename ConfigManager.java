package com.venice.venicehack.core;

import com.google.gson.*;
import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.setting.*;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final Path file = FabricLoader.getInstance().getConfigDir().resolve("venicehack.json");
    private final Path profiles = FabricLoader.getInstance().getConfigDir().resolve("venicehack-profiles");

    public void save() { saveTo(file); }

    public void saveProfile(String name) {
        try { Files.createDirectories(profiles); } catch (Exception ignored) {}
        saveTo(profiles.resolve(name + ".json"));
    }

    public void loadProfile(String name) {
        Path p = profiles.resolve(name + ".json");
        if (Files.exists(p)) readFrom(p);
    }

    private void saveTo(Path p) {
        JsonObject root = new JsonObject();
        for (Module m : VeniceHack.modules.getModules()) {
            JsonObject mod = new JsonObject();
            mod.addProperty("enabled", m.isEnabled());
            mod.addProperty("keybind", m.getKeybind());
            JsonObject sets = new JsonObject();
            for (Setting<?> s : m.getSettings()) {
                if (s instanceof BooleanSetting) sets.addProperty(s.getName(), (Boolean) s.getValue());
                else if (s instanceof NumberSetting || s instanceof KeybindSetting) sets.addProperty(s.getName(), (Number) s.getValue());
                else if (s instanceof StringListSetting) sets.add(s.getName(), new Gson().toJsonTree(s.getValue()));
                else sets.addProperty(s.getName(), String.valueOf(s.getValue()));
            }
            mod.add("settings", sets);
            root.add(m.getName(), mod);
        }
        try { Files.writeString(p, new GsonBuilder().setPrettyPrinting().create().toJson(root)); }
        catch (Exception ignored) {}
    }

    public void load() { if (Files.exists(file)) readFrom(file); }

    private void readFrom(Path p) {
        try {
            JsonObject root = JsonParser.parseString(Files.readString(p)).getAsJsonObject();
            for (Module m : VeniceHack.modules.getModules()) {
                if (!root.has(m.getName())) continue;
                JsonObject mod = root.getAsJsonObject(m.getName());
                if (mod.has("keybind")) m.setKeybind(mod.get("keybind").getAsInt());
                if (mod.has("settings")) {
                    JsonObject sets = mod.getAsJsonObject("settings");
                    for (Setting<?> s : m.getSettings()) {
                        if (!sets.has(s.getName())) continue;
                        JsonElement e = sets.get(s.getName());
                        if (s instanceof BooleanSetting) ((BooleanSetting) s).setValue(e.getAsBoolean());
                        else if (s instanceof NumberSetting) ((NumberSetting) s).setValue(e.getAsDouble());
                        else if (s instanceof KeybindSetting) ((KeybindSetting) s).setValue(e.getAsInt());
                        else if (s instanceof ModeSetting) ((ModeSetting) s).setValue(e.getAsString());
                    }
                }
                if (mod.has("enabled") && mod.get("enabled").getAsBoolean()) m.setEnabled(true);
            }
        } catch (Exception ignored) {}
    }
}
