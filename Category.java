package com.venice.venicehack.core;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), PLAYER("Player"),
    WORLD("World"), MISC("Misc"), EXPLOIT("Exploit"), ANTIBAN("AntiBan");

    public final String label;
    Category(String label) { this.label = label; }
}
