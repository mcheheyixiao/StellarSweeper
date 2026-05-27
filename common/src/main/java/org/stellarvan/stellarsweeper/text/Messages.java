package org.stellarvan.stellarsweeper.text;

import java.util.StringJoiner;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import org.stellarvan.stellarsweeper.cleanup.CleanupReport;
import org.stellarvan.stellarsweeper.cleanup.CleanupStats;

public final class Messages {
    private static final String DIVIDER_TEXT = "------------------------------------";

    private Messages() {
    }

    public static MutableComponent prefix() {
        return Component.literal("[")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal("Stellar").withStyle(ChatFormatting.AQUA))
                .append(Component.literal("Sweeper").withStyle(ChatFormatting.GREEN))
                .append(Component.literal("]").withStyle(ChatFormatting.DARK_AQUA))
                .append(Component.literal(" "));
    }

    public static MutableComponent prefixed(String key, Object... args) {
        return withPrefix(Component.translatable(key, args).withStyle(ChatFormatting.WHITE));
    }

    public static MutableComponent success(String key, Object... args) {
        return withPrefix(Component.translatable(key, args).withStyle(ChatFormatting.GREEN));
    }

    public static MutableComponent info(String key, Object... args) {
        return withPrefix(Component.translatable(key, args).withStyle(ChatFormatting.AQUA));
    }

    public static MutableComponent warning(String key, Object... args) {
        return withPrefix(Component.translatable(key, args).withStyle(ChatFormatting.YELLOW));
    }

    public static MutableComponent errorText(String key, Object... args) {
        return withPrefix(Component.translatable(key, args).withStyle(ChatFormatting.RED));
    }

    public static MutableComponent divider() {
        return Component.literal(DIVIDER_TEXT).withStyle(ChatFormatting.DARK_AQUA);
    }

    public static MutableComponent sectionHeader(String key) {
        return Component.literal("-- ")
                .withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.translatable(key).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" --").withStyle(ChatFormatting.DARK_AQUA));
    }

    public static MutableComponent commandLine(String command, String descriptionKey, Object... descriptionArgs) {
        return Component.literal("  ")
                .append(suggestCommand(command))
                .append(Component.literal("  -  ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(descriptionKey, descriptionArgs).withStyle(ChatFormatting.GRAY));
    }

    public static MutableComponent suggestCommand(String command) {
        int parameterStart = command.indexOf(" <");
        int optionalStart = command.indexOf(" [");
        if (parameterStart < 0 || (optionalStart >= 0 && optionalStart < parameterStart)) {
            parameterStart = optionalStart;
        }

        String base = parameterStart >= 0 ? command.substring(0, parameterStart) : command;
        String parameters = parameterStart >= 0 ? command.substring(parameterStart) : "";

        MutableComponent commandComponent = Component.literal(base)
                .withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("stellarsweeper.hover.suggest_command", command)
                        )));
        if (!parameters.isEmpty()) {
            commandComponent.append(Component.literal(parameters).withStyle(ChatFormatting.GREEN));
        }
        return commandComponent;
    }

    public static MutableComponent listEntry(String listName, int itemCount, boolean active) {
        MutableComponent line = Component.literal(active ? "* " : "- ")
                .withStyle(active ? ChatFormatting.GREEN : ChatFormatting.GRAY);
        line.append(Component.literal(listName).withStyle(active ? ChatFormatting.GREEN : ChatFormatting.AQUA));
        line.append(Component.literal(" (").withStyle(ChatFormatting.GRAY));
        line.append(Component.translatable("stellarsweeper.lists.count", itemCount).withStyle(ChatFormatting.GRAY));
        line.append(Component.literal(")").withStyle(ChatFormatting.GRAY));
        return line;
    }

    public static MutableComponent listItemEntry(String itemId) {
        return Component.literal("- ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(itemId).withStyle(style -> style
                        .withColor(ChatFormatting.AQUA)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sweep remove " + itemId))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.translatable("stellarsweeper.hover.remove_item", itemId)
                        ))));
    }

    public static MutableComponent listSwitchEntry(String listName, int itemCount, boolean active) {
        return listEntry(listName, itemCount, active).withStyle(style -> style
                .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/sweep list-use " + listName))
                .withHoverEvent(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        Component.translatable("stellarsweeper.hover.switch_list", listName)
                )));
    }

    public static void success(CommandSourceStack source, String key, boolean broadcastToOps, Object... args) {
        source.sendSuccess(() -> success(key, args), broadcastToOps);
    }

    public static void info(CommandSourceStack source, String key, boolean broadcastToOps, Object... args) {
        source.sendSuccess(() -> info(key, args), broadcastToOps);
    }

    public static void warning(CommandSourceStack source, String key, boolean broadcastToOps, Object... args) {
        source.sendSuccess(() -> warning(key, args), broadcastToOps);
    }

    private static MutableComponent withPrefix(MutableComponent body) {
        return prefix().append(body);
    }

    public static void broadcast(MinecraftServer server, Component text) {
        server.getPlayerList().broadcastSystemMessage(text, false);
    }

    public static void feedback(CommandSourceStack source, String key, boolean broadcastToOps, Object... args) {
        source.sendSuccess(() -> prefixed(key, args), broadcastToOps);
    }

    public static void error(CommandSourceStack source, String key, Object... args) {
        source.sendFailure(errorText(key, args));
    }

    public static void broadcastCleanupReport(MinecraftServer server, CleanupReport report, boolean preview) {
        if (report.skipped()) {
            String worlds = joinWorlds(report);
            if (worlds.isEmpty()) {
                broadcast(server, warning("stellarsweeper.sweep.skipped", Component.translatable("stellarsweeper.skip.reason.gamerule")));
            } else {
                broadcast(server, warning(
                        "stellarsweeper.sweep.skipped",
                        Component.translatable("stellarsweeper.skip.reason.gamerule_worlds", worlds)
                ));
            }
            return;
        }

        if (report.totalCount() <= 0) {
            broadcast(server, withPrefix(Component.translatable("stellarsweeper.sweep.none").withStyle(ChatFormatting.GRAY)));
            return;
        }

        if (preview) {
            broadcast(server, warning("stellarsweeper.sweep.preview", report.totalCount(), report.entityCount()));
        } else {
            broadcast(server, success("stellarsweeper.sweep.done", report.totalCount(), report.entityCount()));
        }

        for (CleanupStats stats : report.statsByItemId().values()) {
            broadcast(server, Component.translatable(
                    "stellarsweeper.sweep.item_line",
                    stats.displayName(),
                    stats.itemId(),
                    stats.count(),
                    stats.entityCount()
            ).withStyle(ChatFormatting.GRAY));
        }
    }

    public static void sendCleanupReportToSource(CommandSourceStack source, CleanupReport report, boolean preview) {
        if (report.skipped()) {
            String worlds = joinWorlds(report);
            if (worlds.isEmpty()) {
                source.sendSuccess(() -> warning(
                        "stellarsweeper.sweep.skipped",
                        Component.translatable("stellarsweeper.skip.reason.gamerule")
                ), false);
            } else {
                source.sendSuccess(() -> warning(
                        "stellarsweeper.sweep.skipped",
                        Component.translatable("stellarsweeper.skip.reason.gamerule_worlds", worlds)
                ), false);
            }
            return;
        }

        if (report.totalCount() <= 0) {
            source.sendSuccess(() -> withPrefix(Component.translatable("stellarsweeper.sweep.none").withStyle(ChatFormatting.GRAY)), false);
            return;
        }

        if (preview) {
            source.sendSuccess(() -> warning("stellarsweeper.sweep.preview", report.totalCount(), report.entityCount()), false);
        } else {
            source.sendSuccess(() -> success("stellarsweeper.sweep.done", report.totalCount(), report.entityCount()), true);
        }
        for (CleanupStats stats : report.statsByItemId().values()) {
            source.sendSuccess(() -> Component.translatable(
                    "stellarsweeper.sweep.item_line",
                    stats.displayName(),
                    stats.itemId(),
                    stats.count(),
                    stats.entityCount()
            ).withStyle(ChatFormatting.GRAY), false);
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
