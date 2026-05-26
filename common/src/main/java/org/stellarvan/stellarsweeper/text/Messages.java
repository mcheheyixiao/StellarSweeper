package org.stellarvan.stellarsweeper.text;

import java.util.StringJoiner;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.stellarvan.stellarsweeper.cleanup.CleanupReport;
import org.stellarvan.stellarsweeper.cleanup.CleanupStats;

public final class Messages {
    private Messages() {
    }

    public static MutableComponent prefix() {
        return Component.translatable("stellarsweeper.prefix");
    }

    public static MutableComponent prefixed(String key, Object... args) {
        return prefix().append(Component.literal(" ")).append(Component.translatable(key, args));
    }

    public static void broadcast(MinecraftServer server, Component text) {
        server.getPlayerList().broadcastSystemMessage(text, false);
    }

    public static void feedback(CommandSourceStack source, String key, boolean broadcastToOps, Object... args) {
        source.sendSuccess(() -> prefixed(key, args), broadcastToOps);
    }

    public static void error(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(prefixed(key, args));
    }

    public static void broadcastCleanupReport(MinecraftServer server, CleanupReport report, boolean preview) {
        if (report.skipped()) {
            String worlds = joinWorlds(report);
            if (worlds.isEmpty()) {
                broadcast(server, prefixed("stellarsweeper.sweep.skipped", Component.translatable("stellarsweeper.skip.reason.gamerule")));
            } else {
                broadcast(server, prefixed(
                        "stellarsweeper.sweep.skipped",
                        Component.translatable("stellarsweeper.skip.reason.gamerule_worlds", worlds)
                ));
            }
            return;
        }

        if (report.totalCount() <= 0) {
            broadcast(server, prefixed("stellarsweeper.sweep.none"));
            return;
        }

        if (preview) {
            broadcast(server, prefixed("stellarsweeper.sweep.preview", report.totalCount(), report.entityCount()));
        } else {
            broadcast(server, prefixed("stellarsweeper.sweep.done", report.totalCount(), report.entityCount()));
        }

        for (CleanupStats stats : report.statsByItemId().values()) {
            broadcast(server, Component.translatable(
                    "stellarsweeper.sweep.item_line",
                    stats.displayName(),
                    stats.itemId(),
                    stats.count(),
                    stats.entityCount()
            ));
        }
    }

    public static void sendCleanupReportToSource(CommandSourceStack source, CleanupReport report, boolean preview) {
        if (report.skipped()) {
            String worlds = joinWorlds(report);
            if (worlds.isEmpty()) {
                source.sendSuccess(() -> prefixed("stellarsweeper.sweep.skipped", Component.translatable("stellarsweeper.skip.reason.gamerule")), false);
            } else {
                source.sendSuccess(() -> prefixed(
                        "stellarsweeper.sweep.skipped",
                        Component.translatable("stellarsweeper.skip.reason.gamerule_worlds", worlds)
                ), false);
            }
            return;
        }

        if (report.totalCount() <= 0) {
            source.sendSuccess(() -> prefixed("stellarsweeper.sweep.none"), false);
            return;
        }

        if (preview) {
            source.sendSuccess(() -> prefixed("stellarsweeper.sweep.preview", report.totalCount(), report.entityCount()), false);
        } else {
            source.sendSuccess(() -> prefixed("stellarsweeper.sweep.done", report.totalCount(), report.entityCount()), true);
        }
        for (CleanupStats stats : report.statsByItemId().values()) {
            source.sendSuccess(() -> Component.translatable(
                    "stellarsweeper.sweep.item_line",
                    stats.displayName(),
                    stats.itemId(),
                    stats.count(),
                    stats.entityCount()
            ), false);
        }
    }

    private static String joinWorlds(CleanupReport report) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String worldId : report.skippedWorlds()) {
            joiner.add(worldId);
        }
        return joiner.toString();
    }
}
