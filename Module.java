package com.venice.venicehack.core;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.venice.venicehack.setting.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    protected final Minecraft mc = Minecraft.getInstance();
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private int keybind = 0;
    public boolean expanded = false;

    public Module(String name, String description, Category category) {
        this.name = name; this.description = description; this.category = category;
    }
    public Module(String name, Category category) { this(name, "", category); }

    public void onEnable() {}
    public void onDisable() {}
    public void onTick() {}
    public void onRender3D(PoseStack pose, VertexConsumer vc, float partialTick) {}
    public void onRender2D(GuiGraphicsExtractor gfx, float partialTick) {}

    public void toggle() { setEnabled(!enabled); }
    public void setEnabled(boolean v) {
        if (this.enabled == v) return;
        this.enabled = v;
        try { if (v) onEnable(); else onDisable(); } catch (Throwable ignored) {}
    }
    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Category getCategory() { return category; }
    public int getKeybind() { return keybind; }
    public void setKeybind(int k) { this.keybind = k; }
    public List<Setting<?>> getSettings() { return settings; }

    protected void add(Setting<?> s) { s.setParent(this); settings.add(s); }
    protected boolean nullCheck() { return mc.player == null || mc.level == null; }
}
