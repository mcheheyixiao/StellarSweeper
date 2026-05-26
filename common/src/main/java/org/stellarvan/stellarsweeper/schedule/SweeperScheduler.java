package org.stellarvan.stellarsweeper.schedule;

import net.minecraft.server.MinecraftServer;
import org.stellarvan.stellarsweeper.cleanup.CleanupCause;
import org.stellarvan.stellarsweeper.cleanup.CleanupReport;
import org.stellarvan.stellarsweeper.cleanup.CleanupService;
import org.stellarvan.stellarsweeper.config.ConfigManager;
import org.stellarvan.stellarsweeper.config.SweeperConfig;
import org.stellarvan.stellarsweeper.text.Messages;

public final class SweeperScheduler {
    private final ConfigManager configManager;
    private final CleanupService cleanupService;
    private final ThresholdPromptManager thresholdPromptManager;
    private long autoElapsedTicks;
    private long thresholdElapsedTicks;

    public SweeperScheduler(
            ConfigManager configManager,
            CleanupService cleanupService,
            ThresholdPromptManager thresholdPromptManager
    ) {
        this.configManager = configManager;
        this.cleanupService = cleanupService;
        this.thresholdPromptManager = thresholdPromptManager;
    }

    public void onServerTick(MinecraftServer server) {
        if (server == null) {
            return;
        }

        SweeperConfig config = configManager.get();
        long nowTick = server.getTickCount();
        thresholdPromptManager.cleanupExpired(nowTick);

        if (config.enableAutoCleanup) {
            autoElapsedTicks++;
            long interval = config.cleanupInterval;
            long remaining = interval - autoElapsedTicks;
            if (remaining == 1200) {
                Messages.broadcast(server, Messages.prefixed("stellarsweeper.warning.1min"));
            } else if (remaining == 600) {
                Messages.broadcast(server, Messages.prefixed("stellarsweeper.warning.30s"));
            } else if (remaining == 100) {
                Messages.broadcast(server, Messages.prefixed("stellarsweeper.warning.5s"));
            }

            if (autoElapsedTicks >= interval) {
                CleanupReport report = cleanupService.sweep(server, CleanupCause.AUTO_SCHEDULE, true);
                Messages.broadcastCleanupReport(server, report, false);
                autoElapsedTicks = 0;
            }
        } else {
            autoElapsedTicks = 0;
        }

        if (config.enableThresholdCheck) {
            thresholdElapsedTicks++;
            if (thresholdElapsedTicks >= config.thresholdCheckInterval) {
                thresholdElapsedTicks = 0;
                CleanupReport preview = cleanupService.preview(server, CleanupCause.THRESHOLD_CHECK);
                if (preview.totalCount() >= config.itemThreshold
                        && thresholdPromptManager.canPrompt(nowTick, config.warningCooldown)) {
                    long expireTick = nowTick + Math.max(config.warningCooldown, 200);
                    thresholdPromptManager.sendThresholdPrompt(server, preview.totalCount(), nowTick, expireTick);
                }
            }
        } else {
            thresholdElapsedTicks = 0;
        }
    }
}
