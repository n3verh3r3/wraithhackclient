package com.venice.venicehack.setting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class StringListSetting extends Setting<List<String>> {
    public StringListSetting(String n, String... defaults) { super(n, new ArrayList<>(Arrays.asList(defaults))); }
    public List<String> getValues() { return getValue(); }
    @Override public String display() { return getValue().size() + " items"; }
}
