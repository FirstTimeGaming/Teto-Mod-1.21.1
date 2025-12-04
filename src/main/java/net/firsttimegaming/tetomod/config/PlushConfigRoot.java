package net.firsttimegaming.tetomod.config;

import java.util.HashMap;
import java.util.Map;

public class PlushConfigRoot {
    public Map<String, PlushTierConfig> tiers = new HashMap<>();

    public Map<String, Integer> tierLocks = new HashMap<>();

    public PlushConfigRoot() {}
}
