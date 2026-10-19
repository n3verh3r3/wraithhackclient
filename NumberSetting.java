package com.venice.venicehack.setting;
public class NumberSetting extends Setting<Double> {
    private final double min, max, step;
    public NumberSetting(String n, double d, double min, double max, double step) {
        super(n, d); this.min = min; this.max = max; this.step = step;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
    @Override public void setValue(Double v) {
        v = Math.round(v / step) * step;
        super.setValue(Math.max(min, Math.min(max, v)));
    }
    public int getInt() { return (int) Math.round(getValue()); }
    public float getFloat() { return getValue().floatValue(); }
    @Override public String display() {
        double v = getValue();
        return v == Math.floor(v) ? String.valueOf((int) v) : String.format("%.2f", v);
    }
}
