package com.venice.venicehack;

import com.mojang.blaze3d.platform.InputConstants;
import com.venice.venicehack.core.*;
import com.venice.venicehack.ui.ClickGui;
import com.venice.venicehack.ui.HudManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class VeniceHack implements ClientModInitializer {
    public static final String NAME = "VeniceHack";
    public static final String VERSION = "2.0.0";
    public static VeniceHack INSTANCE;

    public static ModuleManager modules;
    public static ConfigManager config;
    public static HudManager hud;
    public static CommandManager commands;
    public static FriendManager friends;
    public static NotificationManager notifications;
    public static KeyMapping guiKey;

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("venicehack", path);
    }

    @Override
    public void onInitializeClient() {
        INSTANCE = this;
        modules = new ModuleManager();
        config = new ConfigManager();
        hud = new HudManager();
        commands = new CommandManager();
        friends = new FriendManager();
        notifications = new NotificationManager();
        config.load();

        guiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.venicehack.gui", InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT, "VeniceHack"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (guiKey.consumeClick()) client.setScreen(new ClickGui());
            modules.onTick();
            notifications.onTick();
        });

        WorldRenderEvents.END.register(ctx -> {
            if (ctx.consumers() == null || ctx.matrixStack() == null) return;
            modules.onRender3D(ctx);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, id("hud"),
                (graphics, deltaTracker) -> hud.render(graphics));

        Runtime.getRuntime().addShutdownHook(new Thread(config::save));
    }
}
