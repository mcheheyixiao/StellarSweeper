package org.stellarvan.stellarsweeper.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SweeperConfig {
    public boolean enableAutoCleanup = true;
    public boolean enableThresholdCheck = true;
    public int cleanupInterval = 12000;
    public int thresholdCheckInterval = 100;
    public int warningCooldown = 1200;
    public int cleanRadius = 48;
    public int yMin = -64;
    public int yMax = 320;
    public int itemThreshold = 100;
    public String language = "zh_cn";
    public String currentCleanupList = "default";
    public Map<String, List<String>> cleanupLists = new LinkedHashMap<>();

    public static SweeperConfig createDefault() {
        SweeperConfig config = new SweeperConfig();
        config.cleanupLists.put("default", new ArrayList<>(List.of(
                "minecraft:cobblestone",
                "minecraft:dirt",
                "minecraft:bone",
                "minecraft:rotten_flesh",
                "minecraft:spider_eye",
                "minecraft:string",
                "minecraft:feather",
                "minecraft:gunpowder",
                "minecraft:flint",
                "minecraft:gravel",
                "minecraft:sand",
                "minecraft:clay_ball",
                "minecraft:snowball",
                "minecraft:egg",
                "minecraft:cod",
                "minecraft:salmon",
                "minecraft:pufferfish",
                "minecraft:tropical_fish",
                "minecraft:stick",
                "minecraft:stone"
        )));
        config.cleanupLists.put("NowClean", new ArrayList<>(List.of(
                "minecraft:gravel",
                "minecraft:sand"
        )));
        return config;
    }

    public List<String> getActiveCleanupList() {
        List<String> active = cleanupLists.get(currentCleanupList);
        if (active != null) {
            return active;
        }
        if (cleanupLists.isEmpty()) {
            cleanupLists.put("default", new ArrayList<>());
            currentCleanupList = "default";
            return cleanupLists.get("default");
        }
        String first = cleanupLists.keySet().iterator().next();
        currentCleanupList = first;
        return cleanupLists.get(first);
    }
}
