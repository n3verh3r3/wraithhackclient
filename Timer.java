package com.venice.venicehack.util;
public class Timer {
    private long last = System.nanoTime();
    public boolean passed(double ms) { return (System.nanoTime() - last) / 1000000.0 >= ms; }
    public void reset() { last = System.nanoTime(); }
    public boolean passedAndReset(double ms) { if (passed(ms)) { reset(); return true; } return false; }
}
