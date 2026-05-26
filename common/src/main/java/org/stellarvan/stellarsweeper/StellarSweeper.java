package org.stellarvan.stellarsweeper;

import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.TickEvent;
import org.stellarvan.stellarsweeper.cleanup.CleanupService;
import org.stellarvan.stellarsweeper.command.SweepCommands;
import org.stellarvan.stellarsweeper.config.ConfigManager;
import org.stellarvan.stellarsweeper.scan.ItemScanner;
import org.stellarvan.stellarsweeper.schedule.SweeperScheduler;
import org.stellarvan.stellarsweeper.schedule.ThresholdPromptManager;

public final class StellarSweeper {
    private static boolean initialized;
    private static ConfigManager configManager;
    private static CleanupService cleanupService;
    private static ThresholdPromptManager thresholdPromptManager;
    private static SweeperScheduler scheduler;

    private StellarSweeper() {
    }

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        configManager = new ConfigManager();
        configManager.load();

        cleanupService = new CleanupService(configManager, new ItemScanner());
        thresholdPromptManager = new ThresholdPromptManager();
        scheduler = new SweeperScheduler(configManager, cleanupService, thresholdPromptManager);

        CommandRegistrationEvent.EVENT.register((dispatcher, registryAccess, environment) -> SweepCommands.register(dispatcher));
        TickEvent.SERVER_POST.register(server -> scheduler.onServerTick(server));

        Constants.LOGGER.info("Initialized StellarSweeper on platform '{}'.", platform().getPlatformName());
    }

    public static ConfigManager configManager() {
        return configManager;
    }

    public static CleanupService cleanupService() {
        return cleanupService;
    }

    public static ThresholdPromptManager thresholdPromptManager() {
        return thresholdPromptManager;
    }

    public static SweeperScheduler scheduler() {
        return scheduler;
    }

    private static org.stellarvan.stellarsweeper.platform.PlatformBridge platform() {
        return org.stellarvan.stellarsweeper.platform.Services.platform();
    }
}
