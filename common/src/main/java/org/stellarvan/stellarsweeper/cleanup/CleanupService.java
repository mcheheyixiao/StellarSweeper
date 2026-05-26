package org.stellarvan.stellarsweeper.cleanup;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.GameRules;
import org.stellarvan.stellarsweeper.config.ConfigManager;
import org.stellarvan.stellarsweeper.config.SweeperConfig;
import org.stellarvan.stellarsweeper.rule.CleanupContext;
import org.stellarvan.stellarsweeper.rule.CleanupDecision;
import org.stellarvan.stellarsweeper.rule.RuleEvaluator;
import org.stellarvan.stellarsweeper.scan.DroppedItemSnapshot;
import org.stellarvan.stellarsweeper.scan.ItemScanner;
import org.stellarvan.stellarsweeper.scan.ScanResult;
import org.stellarvan.stellarsweeper.scan.ScanScope;

public final class CleanupService {
    private final ConfigManager configManager;
    private final ItemScanner scanner;

    public CleanupService(ConfigManager configManager, ItemScanner scanner) {
        this.configManager = configManager;
        this.scanner = scanner;
    }

    public CleanupReport preview(MinecraftServer server, CleanupCause cause) {
        return collect(server, cause, false);
    }

    public CleanupReport sweep(MinecraftServer server, CleanupCause cause, boolean notify) {
        return collect(server, cause, true);
    }

    private CleanupReport collect(MinecraftServer server, CleanupCause cause, boolean removeMatchedEntities) {
        SweeperConfig config = configManager.get();
        ScanResult scanResult = scanner.scan(server, ScanScope.fromConfig(config));
        CleanupReport report = new CleanupReport(cause);
        if (!scanResult.hasOnlinePlayers()) {
            return report;
        }

        RuleEvaluator evaluator = new RuleEvaluator(new HashSet<>(config.getActiveCleanupList()));
        List<DroppedItemSnapshot> candidates = scanResult.candidates();
        for (DroppedItemSnapshot snapshot : candidates) {
            CleanupDecision decision = evaluator.evaluate(new CleanupContext(
                    snapshot.entity(),
                    snapshot.stack(),
                    snapshot.itemId()
            ));
            if (!decision.shouldClean()) {
                continue;
            }

            if (!snapshot.level().getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)) {
                report.addSkippedWorld(snapshot.level().dimension().location().toString());
                continue;
            }

            report.addItem(snapshot.itemId(), snapshot.displayName(), snapshot.stack().getCount());
            if (removeMatchedEntities) {
                ItemEntity entity = snapshot.entity();
                if (entity != null && entity.isAlive()) {
                    entity.discard();
                }
            }
        }

        if (report.totalCount() == 0 && !report.skippedWorlds().isEmpty()) {
            report.setSkipped("do_entity_drops_disabled");
        }
        return report;
    }
}
