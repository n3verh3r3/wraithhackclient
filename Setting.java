package com.venice.venicehack.setting;

import com.venice.venicehack.core.Module;
import java.util.function.BooleanSupplier;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private Module parent;
    private BooleanSupplier visible = () -> true;

    public Setting(String name, T def) { this.name = name; this.value = def; }
    public String getName() { return name; }
    public T getValue() { return value; }
    public void setValue(T v) { this.value = v; }
    public void setParent(Module m) { this.parent = m; }
    public Setting<T> visibleWhen(BooleanSupplier s) { this.visible = s; return this; }
    public boolean isVisible() { return visible.getAsBoolean(); }
    public abstract String display();
}
