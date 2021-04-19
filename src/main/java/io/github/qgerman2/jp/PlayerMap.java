package io.github.qgerman2.jp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PlayerMap<V> extends HashMap<String, V> implements Map<String, V> {
    private final PlayerMap<V> instance;
    private static final Set<String> connectedPlayers = new HashSet<>();
    public PlayerMap() {
        instance = this;
    }
    private boolean connected(String name) {
        if (!connectedPlayers.contains(name)) {
            instance.remove(name);
            return false;
        }
        return true;
    }
    @Override
    public boolean containsKey(Object name) {
        if (connected((String) name)) {
            return super.containsKey(name);
        }
        return false;
    }
    @Override
    public V get(Object name) {
        if (connected((String) name)) {
            return super.get(name);
        }
        return null;
    }
    static public void onPlayerJoin(String name) {
        connectedPlayers.add(name);
    }
    static public void onPlayerQuit(String name) {
        connectedPlayers.remove(name);
    }
}