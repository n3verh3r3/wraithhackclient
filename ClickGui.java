package com.venice.venicehack.ui;

import com.venice.venicehack.VeniceHack;
import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ClickGui extends Screen {
    private Category selected = Category.COMBAT;
    private int scroll = 0;
    private Module expanded = null;
    private final int panelX = 92, panelY = 20, panelW = 250, panelH = 250;

    public ClickGui() { super(Component.literal("VeniceHack")); }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        renderBackground(gfx, mouseX, mouseY, partialTick);
        gfx.fill(0, 0, width, height, 0x66000000);

        gfx.fill(10, panelY, 86, panelY + panelH, 0xDD101018);
        int cy = panelY + 4;
        for (Category c : Category.values()) {
            boolean sel = c == selected;
            gfx.fill(12, cy, 84, cy + 20, sel ? 0xFF3A2A6A : 0xFF1A1A22);
            gfx.drawString(font, c.label, 16, cy + 6, sel ? 0xFFFFAAFF : 0xFFAAAAAA, false);
            cy += 22;
        }

        gfx.fill(panelX, panelY, panelX + panelW, panelY + panelH, 0xDD101018);
        gfx.drawString(font, selected.label + " modules", panelX + 8, panelY + 6, 0xFFD7B3FF, false);

        List<Module> list = VeniceHack.modules.getByCategory(selected);
        int my = panelY + 22 - scroll;
        for (Module m : list) {
            if (my + 18 > panelY && my < panelY + panelH) {
                boolean hover = mouseX >= panelX + 4 && mouseX <= panelX + panelW - 4 && mouseY >= my && mouseY <= my + 18;
                gfx.fill(panelX + 4, my, panelX + panelW - 4, my + 18,
                        m.isEnabled() ? 0xCC2A1A4A : (hover ? 0xCC222233 : 0xAA181820));
                gfx.drawString(font, m.getName(), panelX + 10, my + 5, m.isEnabled() ? 0xFF7FE08A : 0xFFAAAAAA, false);
                gfx.drawString(font, m.isEnabled() ? "ON" : "OFF", panelX + panelW - 34, my + 5,
                        m.isEnabled() ? 0xFF7FE08A : 0xFF555555, false);
            }
            my += 20;
        }

        if (expanded != null) {
            int sx = panelX + panelW + 10;
            gfx.fill(sx, panelY, sx + 210, panelY + panelH, 0xDD101018);
            gfx.drawString(font, expanded.getName(), sx + 8, panelY + 6, 0xFFD7B3FF, false);
            int sy = panelY + 22;
            for (Setting<?> s : expanded.getSettings()) {
                if (!s.isVisible()) continue;
                gfx.drawString(font, s.getName(), sx + 8, sy, 0xFFAAAAAA, false);
                if (s instanceof BooleanSetting bs)
                    gfx.fill(sx + 156, sy - 2, sx + 200, sy + 10, bs.getValue() ? 0xFF3A7A3A : 0xFF7A3A3A);
                else if (s instanceof NumberSetting ns) {
                    int barW = 90;
                    int fillW = (int) (((ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin())) * barW);
                    gfx.fill(sx + 100, sy - 2, sx + 100 + barW, sy + 10, 0xFF222233);
                    gfx.fill(sx + 100, sy - 2, sx + 100 + fillW, sy + 10, 0xFF9A5AFF);
                }
                gfx.drawString(font, s.display(), sx + 156, sy, 0xFFFFFFFF, false);
                sy += 16;
            }
        }
        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        int cy = panelY + 4;
        for (Category c : Category.values()) {
            if (mx >= 12 && mx <= 84 && my >= cy && my <= cy + 20) { selected = c; scroll = 0; return true; }
            cy += 22;
        }
        List<Module> list = VeniceHack.modules.getByCategory(selected);
        int my2 = panelY + 22 - scroll;
        for (Module m : list) {
            if (mx >= panelX + 4 && mx <= panelX + panelW - 4 && my >= my2 && my <= my2 + 18) {
                if (button == 0) m.toggle();
                else if (button == 1) expanded = (expanded == m) ? null : m;
                return true;
            }
            my2 += 20;
        }
        if (expanded != null && button == 0) {
            int sx = panelX + panelW + 10, sy = panelY + 22;
            for (Setting<?> s : expanded.getSettings()) {
                if (!s.isVisible()) continue;
                if (mx >= sx && mx <= sx + 210 && my >= sy - 2 && my <= sy + 12) {
                    if (s instanceof BooleanSetting bs) bs.setValue(!bs.getValue());
                    else if (s instanceof ModeSetting ms) ms.cycle();
                    return true;
                }
                sy += 16;
            }
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double dx, double dy) {
        scroll = Math.max(0, scroll - (int) (dy * 15));
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (button == 0 && expanded != null) {
            int sx = panelX + panelW + 10, sy = panelY + 22;
            for (Setting<?> s : expanded.getSettings()) {
                if (!s.isVisible()) continue;
                if (s instanceof NumberSetting ns && mx >= sx + 100 && mx <= sx + 190 && my >= sy - 4 && my <= sy + 12) {
                    double pct = Math.max(0, Math.min(1, (mx - (sx + 100)) / 90.0));
                    ns.setValue(ns.getMin() + pct * (ns.getMax() - ns.getMin()));
                    return true;
                }
                sy += 16;
            }
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override public boolean isPauseScreen() { return false; }
}
