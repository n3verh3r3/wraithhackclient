package com.venice.venicehack.core;

import java.util.HashSet;
import java.util.Set;

public class FriendManager {
    private final Set<String> friends = new HashSet<>();
    public void add(String n) { friends.add(n.toLowerCase()); }
    public void remove(String n) { friends.remove(n.toLowerCase()); }
    public boolean isFriend(String n) { return friends.contains(n.toLowerCase()); }
    public Set<String> all() { return friends; }
}
