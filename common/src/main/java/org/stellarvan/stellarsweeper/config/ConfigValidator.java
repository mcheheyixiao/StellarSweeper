package org.stellarvan.stellarsweeper.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.stellarvan.stellarsweeper.Constants;

public final class ConfigValidator {
    private ConfigValidator() {
    }

    public static void normalize(SweeperConfig config) {
        if (config.cleanupInterval < 200) {
            config.cleanupInterval = 200;
        }
        if (config.thresholdCheckInterval <= 0) {
            config.thresholdCheckInterval = 100;
        }
        if (config.warningCooldown < 0) {
            config.warningCooldown = 0;
        }
        if (config.cleanRadius < 1) {
            config.cleanRadius = 1;
        }
        if (config.itemThreshold < 1) {
            config.itemThreshold = 1;
        }
        if (config.yMin >= config.yMax) {
            config.yMin = -64;
            config.yMax = 320;
        }

        if (config.cleanupLists == null) {
            config.cleanupLists = new LinkedHashMap<>();
        }
        if (config.cleanupLists.isEmpty()) {
            config.cleanupLists.put("default", new ArrayList<>());
        }

        normalizeLists(config.cleanupLists);

        if (config.currentCleanupList == null || !config.cleanupLists.containsKey(config.currentCleanupList)) {
            if (config.cleanupLists.containsKey("default")) {
                config.currentCleanupList = "default";
            } else {
                config.currentCleanupList = config.cleanupLists.keySet().iterator().next();
            }
        }
    }

    private static void normalizeLists(Map<String, List<String>> cleanupLists) {
        List<String> names = new ArrayList<>(cleanupLists.keySet());
        for (String listName : names) {
            List<String> ids = cleanupLists.get(listName);
            if (ids == null) {
                cleanupLists.put(listName, new ArrayList<>());
                continue;
            }
            LinkedHashSet<String> deduped = new LinkedHashSet<>();
            for (String rawId : ids) {
                if (rawId == null || rawId.isBlank()) {
                    continue;
                }
                String itemId = rawId.trim();
                ResourceLocation location = ResourceLocation.tryParse(itemId);
                if (location == null) {
                    Constants.LOGGER.warn("Invalid item id in cleanup list '{}': {}", listName, itemId);
                    deduped.add(itemId);
                    continue;
                }
                if (!BuiltInRegistries.ITEM.containsKey(location)) {
                    Constants.LOGGER.warn("Unknown item id in cleanup list '{}': {}", listName, itemId);
                }
                deduped.add(itemId);
            }
            cleanupLists.put(listName, new ArrayList<>(deduped));
        }
    }
}
