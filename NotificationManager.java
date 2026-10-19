package com.venice.venicehack.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class NotificationManager {
    public record Toast(String text, long born, int color) {}
    public final List<Toast> toasts = new ArrayList<>();

    public void push(String text) { push(text, 0xFFAA55FF); }
    public void push(String text, int color) { toasts.add(new Toast(text, System.currentTimeMillis(), color)); }

    public void onTick() { toasts.removeIf(t -> System.currentTimeMillis() - t.born() > 3000); }

    public void render(GuiGraphicsExtractor gfx) {
        Minecraft mc = Minecraft.getInstance();
        int y = 60;
        for (Toast t : toasts) {
            gfx.text(mc.font, Component.literal(t.text()), 4, y, t.color(), true);
            y += 12;
        }
    }
}
