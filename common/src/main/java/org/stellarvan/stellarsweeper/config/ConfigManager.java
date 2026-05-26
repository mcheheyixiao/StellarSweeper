package org.stellarvan.stellarsweeper.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.stellarvan.stellarsweeper.Constants;
import org.stellarvan.stellarsweeper.platform.Services;

public final class ConfigManager {
    private static final DateTimeFormatter BROKEN_SUFFIX = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private SweeperConfig config;

    public synchronized SweeperConfig load() {
        Path path = getConfigPath();
        ensureConfigDir(path.getParent());
        if (!Files.exists(path)) {
            config = SweeperConfig.createDefault();
            ConfigValidator.normalize(config);
            save();
            return config;
        }

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            SweeperConfig loaded = gson.fromJson(reader, SweeperConfig.class);
            if (loaded == null) {
                throw new IllegalStateException("Config file parsed as null.");
            }
            config = loaded;
            ConfigValidator.normalize(config);
            save();
        } catch (Exception exception) {
            backupBrokenFile(path);
            config = SweeperConfig.createDefault();
            ConfigValidator.normalize(config);
            save();
            Constants.LOGGER.warn("Failed to load config '{}', restored default.", path, exception);
        }
        return config;
    }

    public synchronized SweeperConfig reload() {
        return load();
    }

    public synchronized void save() {
        if (config == null) {
            config = SweeperConfig.createDefault();
        }
        ConfigValidator.normalize(config);
        Path path = getConfigPath();
        ensureConfigDir(path.getParent());
        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        } catch (IOException exception) {
            Constants.LOGGER.error("Failed to save config '{}'.", path, exception);
        }
    }

    public synchronized SweeperConfig get() {
        if (config == null) {
            return load();
        }
        return config;
    }

    public synchronized boolean createList(String listName) {
        SweeperConfig current = get();
        if (current.cleanupLists.containsKey(listName)) {
            return false;
        }
        current.cleanupLists.put(listName, new ArrayList<>());
        save();
        return true;
    }

    public synchronized DeleteListResult deleteList(String listName) {
        SweeperConfig current = get();
        if (!current.cleanupLists.containsKey(listName)) {
            return DeleteListResult.NOT_FOUND;
        }
        if (current.cleanupLists.size() <= 1) {
            return DeleteListResult.CANNOT_DELETE_LAST;
        }
        if (listName.equals(current.currentCleanupList)) {
            return DeleteListResult.CANNOT_DELETE_CURRENT;
        }
        current.cleanupLists.remove(listName);
        save();
        return DeleteListResult.DELETED;
    }

    public synchronized boolean useList(String listName) {
        SweeperConfig current = get();
        if (!current.cleanupLists.containsKey(listName)) {
            return false;
        }
        current.currentCleanupList = listName;
        save();
        return true;
    }

    public synchronized AddItemResult addItemToActiveList(String itemId) {
        List<String> active = get().getActiveCleanupList();
        if (active.contains(itemId)) {
            return AddItemResult.ALREADY_EXISTS;
        }
        active.add(itemId);
        save();
        return AddItemResult.ADDED;
    }

    public synchronized boolean removeItemFromActiveList(String itemId) {
        boolean removed = get().getActiveCleanupList().remove(itemId);
        if (removed) {
            save();
        }
        return removed;
    }

    public enum DeleteListResult {
        DELETED,
        NOT_FOUND,
        CANNOT_DELETE_CURRENT,
        CANNOT_DELETE_LAST
    }

    public enum AddItemResult {
        ADDED,
        ALREADY_EXISTS
    }

    private Path getConfigPath() {
        return Services.platform().getConfigDir().resolve(Constants.CONFIG_FILE_NAME);
    }

    private void ensureConfigDir(Path configDir) {
        if (configDir == null) {
            return;
        }
        try {
            Files.createDirectories(configDir);
        } catch (IOException exception) {
            Constants.LOGGER.error("Failed to create config directory '{}'.", configDir, exception);
        }
    }

    private void backupBrokenFile(Path path) {
        if (!Files.exists(path)) {
            return;
        }
        String suffix = LocalDateTime.now().format(BROKEN_SUFFIX);
        Path backup = path.resolveSibling(Constants.CONFIG_FILE_NAME + ".broken." + suffix);
        try {
            Files.move(path, backup, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException moveEx) {
            Constants.LOGGER.error("Failed to backup broken config from '{}' to '{}'.", path, backup, moveEx);
        }
    }
}
