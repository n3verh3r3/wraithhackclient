package com.venice.venicehack.modules;

import com.venice.venicehack.core.Category;
import com.venice.venicehack.core.Module;
import com.venice.venicehack.setting.NumberSetting;

public final class WorldModules {
    private WorldModules() {}

    public static class Timer extends Module {
        private final NumberSetting speed = new NumberSetting("Speed", 1.2, 0.1, 5.0, 0.05);
        public Timer() { super("Timer", "Game tick speed.", Category.WORLD); add(speed); }
        public float getSpeed() { return speed.getFloat(); }
    }

    public static class AutoSign extends Module { public AutoSign() { super("AutoSign", "Auto-writes sign text.", Category.WORLD); } }
    public static class Excavator extends Module { public Excavator() { super("Excavator", "Digs a tunnel ahead.", Category.WORLD); } }
    public static class Flattener extends Module { public Flattener() { super("Flattener", "Flattens around you.", Category.WORLD); } }
    public static class BuildRandom extends Module { public BuildRandom() { super("BuildRandom", "Random bridge builder.", Category.WORLD); } }
    public static class StashFinder extends Module { public StashFinder() { super("StashFinder", "Alerts on chest clusters.", Category.WORLD); } }
    public static class TpsSync extends Module { public TpsSync() { super("TpsSync", "Sync timing to server TPS.", Category.WORLD); } }
    public static class AutoSell extends Module { public AutoSell() { super("AutoSell", "Sells to a shop NPC.", Category.WORLD); } }
    public static class AutoSmelt extends Module { public AutoSmelt() { super("AutoSmelt", "Feeds furnaces.", Category.WORLD); } }
    public static class ChestScaffold extends Module { public ChestScaffold() { super("ChestScaffold", "Scaffolds while looting.", Category.WORLD); } }
}
