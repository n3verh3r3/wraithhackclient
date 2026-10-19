package com.venice.venicehack.setting;
import java.util.Arrays;
import java.util.List;
public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    public ModeSetting(String n, String d, String... modes) { super(n, d); this.modes = Arrays.asList(modes); }
    public boolean is(String m) { return getValue().equalsIgnoreCase(m); }
    public void cycle() { int i = modes.indexOf(getValue()); setValue(modes.get((i + 1) % modes.size())); }
    @Override public String display() { return getValue(); }
}
