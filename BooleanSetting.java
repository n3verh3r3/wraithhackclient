package com.venice.venicehack.setting;
public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String n, boolean d) { super(n, d); }
    @Override public String display() { return getValue() ? "ON" : "OFF"; }
}
