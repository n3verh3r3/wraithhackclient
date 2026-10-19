package com.venice.venicehack.setting;
import com.mojang.blaze3d.platform.InputConstants;
public class KeybindSetting extends Setting<Integer> {
    public KeybindSetting(String n, int d) { super(n, d); }
    @Override public String display() {
        return getValue() == 0 ? "NONE" : InputConstants.getKey(getValue(), 0).getDisplayName().getString();
    }
}
