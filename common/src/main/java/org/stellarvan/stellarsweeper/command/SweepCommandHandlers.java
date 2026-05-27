package org.stellarvan.stellarsweeper.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.stellarvan.stellarsweeper.StellarSweeper;
import org.stellarvan.stellarsweeper.cleanup.CleanupCause;
import org.stellarvan.stellarsweeper.cleanup.CleanupReport;
import org.stellarvan.stellarsweeper.config.ConfigManager;
import org.stellarvan.stellarsweeper.config.SweeperConfig;
import org.stellarvan.stellarsweeper.schedule.ThresholdPromptManager;
import org.stellarvan.stellarsweeper.text.Messages;

public final class SweepCommandHandlers {
    private static final SimpleCommandExceptionType PLAYER_ONLY = new SimpleCommandExceptionType(
            Messages.prefixed("stellarsweeper.player_only")
    );

    private SweepCommandHandlers() {
    }

    public static int showShortHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Messages.info(
                "stellarsweeper.help.short",
                Messages.suggestCommand("/sweep help")
        ), false);
        source.sendSuccess(() -> Messages.info(
                "stellarsweeper.help.short.common",
                Messages.suggestCommand("/sweep preview"),
                Messages.suggestCommand("/sweep run")
        ), false);
        return 1;
    }

    public static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Messages.info("stellarsweeper.help.header"), false);
        source.sendSuccess(Messages::divider, false);

        source.sendSuccess(() -> Messages.sectionHeader("stellarsweeper.help.common"), false);
        sendCommandLine(source, "/sweep run", "stellarsweeper.help.desc.run");
        sendCommandLine(source, "/sweep preview", "stellarsweeper.help.desc.preview");
        sendCommandLine(source, "/sweep reload", "stellarsweeper.help.desc.reload");
        sendCommandLine(source, "/sweep save", "stellarsweeper.help.desc.save");

        source.sendSuccess(() -> Messages.sectionHeader("stellarsweeper.help.automation"), false);
        sendCommandLine(source, "/sweep toggle auto", "stellarsweeper.help.desc.toggle_auto");
        sendCommandLine(source, "/sweep toggle threshold", "stellarsweeper.help.desc.toggle_threshold");
        sendCommandLine(source, "/sweep set interval <ticks>", "stellarsweeper.help.desc.set_interval");
        sendCommandLine(source, "/sweep set threshold <count>", "stellarsweeper.help.desc.set_threshold");
        sendCommandLine(source, "/sweep set radius <blocks>", "stellarsweeper.help.desc.set_radius");
        sendCommandLine(source, "/sweep set y <min> <max>", "stellarsweeper.help.desc.set_y");

        source.sendSuccess(() -> Messages.sectionHeader("stellarsweeper.help.lists"), false);
        sendCommandLine(source, "/sweep list [listName]", "stellarsweeper.help.desc.list");
        sendCommandLine(source, "/sweep lists", "stellarsweeper.help.desc.lists");
        sendCommandLine(source, "/sweep list-create <listName>", "stellarsweeper.help.desc.list_create");
        sendCommandLine(source, "/sweep list-delete <listName>", "stellarsweeper.help.desc.list_delete");
        sendCommandLine(source, "/sweep list-use <listName>", "stellarsweeper.help.desc.list_use");

        source.sendSuccess(() -> Messages.sectionHeader("stellarsweeper.help.items"), false);
        sendCommandLine(source, "/sweep add", "stellarsweeper.help.desc.add_held");
        sendCommandLine(source, "/sweep add <itemId>", "stellarsweeper.help.desc.add_id");
        sendCommandLine(source, "/sweep remove <itemId>", "stellarsweeper.help.desc.remove");

        source.sendSuccess(Messages::divider, false);
        source.sendSuccess(() -> Messages.warning("stellarsweeper.help.tip_click"), false);
        return 1;
    }

    public static int runSweep(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CleanupReport report = StellarSweeper.cleanupService().sweep(source.getServer(), CleanupCause.COMMAND_RUN, true);
        Messages.broadcastCleanupReport(source.getServer(), report, false);
        if (!(source.getEntity() instanceof ServerPlayer)) {
            Messages.sendCleanupReportToSource(source, report, false);
        }
        return 1;
    }

    public static int previewSweep(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        CleanupReport report = StellarSweeper.cleanupService().preview(source.getServer(), CleanupCause.COMMAND_PREVIEW);
        Messages.sendCleanupReportToSource(source, report, true);
        return 1;
    }

    public static int reloadConfig(CommandContext<CommandSourceStack> context) {
        StellarSweeper.configManager().reload();
        Messages.success(context.getSource(), "stellarsweeper.config.reloaded", false);
        return 1;
    }

    public static int saveConfig(CommandContext<CommandSourceStack> context) {
        StellarSweeper.configManager().save();
        Messages.success(context.getSource(), "stellarsweeper.config.saved", false);
        return 1;
    }

    public static int toggleAuto(CommandContext<CommandSourceStack> context) {
        SweeperConfig config = StellarSweeper.configManager().get();
        config.enableAutoCleanup = !config.enableAutoCleanup;
        StellarSweeper.configManager().save();
        if (config.enableAutoCleanup) {
            Messages.success(context.getSource(), "stellarsweeper.toggle.auto.enabled", true);
        } else {
            Messages.warning(context.getSource(), "stellarsweeper.toggle.auto.disabled", true);
        }
        return 1;
    }

    public static int toggleThreshold(CommandContext<CommandSourceStack> context) {
        SweeperConfig config = StellarSweeper.configManager().get();
        config.enableThresholdCheck = !config.enableThresholdCheck;
        StellarSweeper.configManager().save();
        if (config.enableThresholdCheck) {
            Messages.success(context.getSource(), "stellarsweeper.toggle.threshold.enabled", true);
        } else {
            Messages.warning(context.getSource(), "stellarsweeper.toggle.threshold.disabled", true);
        }
        return 1;
    }

    public static int setInterval(CommandContext<CommandSourceStack> context, int ticks) {
        SweeperConfig config = StellarSweeper.configManager().get();
        config.cleanupInterval = ticks;
        StellarSweeper.configManager().save();
        Messages.success(context.getSource(), "stellarsweeper.set.interval", false, ticks);
        return 1;
    }

    public static int setThreshold(CommandContext<CommandSourceStack> context, int count) {
        SweeperConfig config = StellarSweeper.configManager().get();
        config.itemThreshold = count;
        StellarSweeper.configManager().save();
        Messages.success(context.getSource(), "stellarsweeper.set.threshold", false, count);
        return 1;
    }

    public static int setRadius(CommandContext<CommandSourceStack> context, int radius) {
        SweeperConfig config = StellarSweeper.configManager().get();
        config.cleanRadius = radius;
        StellarSweeper.configManager().save();
        Messages.success(context.getSource(), "stellarsweeper.set.radius", false, radius);
        return 1;
    }

    public static int setYRange(CommandContext<CommandSourceStack> context, int min, int max) {
        if (min >= max) {
            Messages.error(context.getSource(), "stellarsweeper.set.y.invalid");
            return 0;
        }
        SweeperConfig config = StellarSweeper.configManager().get();
        config.yMin = min;
        config.yMax = max;
        StellarSweeper.configManager().save();
        Messages.success(context.getSource(), "stellarsweeper.set.y", false, min, max);
        return 1;
    }

    public static int listCurrent(CommandContext<CommandSourceStack> context) {
        SweeperConfig config = StellarSweeper.configManager().get();
        return listNamedInternal(context.getSource(), config.currentCleanupList, config.getActiveCleanupList());
    }

    public static int listNamed(CommandContext<CommandSourceStack> context, String listName) {
        SweeperConfig config = StellarSweeper.configManager().get();
        List<String> target = config.cleanupLists.get(listName);
        if (target == null) {
            Messages.error(context.getSource(), "stellarsweeper.list.not_found", listName);
            return 0;
        }
        return listNamedInternal(context.getSource(), listName, target);
    }

    public static int listAll(CommandContext<CommandSourceStack> context) {
        SweeperConfig config = StellarSweeper.configManager().get();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Messages.info("stellarsweeper.lists.title"), false);
        source.sendSuccess(Messages::divider, false);
        for (String listName : config.cleanupLists.keySet()) {
            List<String> items = config.cleanupLists.get(listName);
            boolean active = listName.equals(config.currentCleanupList);
            source.sendSuccess(
                    () -> Messages.listSwitchEntry(listName, items == null ? 0 : items.size(), active),
                    false
            );
        }
        return 1;
    }

    public static int createList(CommandContext<CommandSourceStack> context, String listName) {
        boolean created = StellarSweeper.configManager().createList(listName);
        if (!created) {
            Messages.error(context.getSource(), "stellarsweeper.list.already_exists", listName);
            return 0;
        }
        Messages.success(context.getSource(), "stellarsweeper.list.created", false, listName);
        return 1;
    }

    public static int deleteList(CommandContext<CommandSourceStack> context, String listName) {
        ConfigManager.DeleteListResult result = StellarSweeper.configManager().deleteList(listName);
        if (result == ConfigManager.DeleteListResult.DELETED) {
            Messages.success(context.getSource(), "stellarsweeper.list.deleted", false, listName);
            return 1;
        }
        if (result == ConfigManager.DeleteListResult.CANNOT_DELETE_CURRENT) {
            Messages.error(context.getSource(), "stellarsweeper.list.cannot_delete_current", listName);
            return 0;
        }
        if (result == ConfigManager.DeleteListResult.CANNOT_DELETE_LAST) {
            Messages.error(context.getSource(), "stellarsweeper.list.cannot_delete_last");
            return 0;
        }
        Messages.error(context.getSource(), "stellarsweeper.list.not_found", listName);
        return 0;
    }

    public static int useList(CommandContext<CommandSourceStack> context, String listName) {
        boolean switched = StellarSweeper.configManager().useList(listName);
        if (!switched) {
            Messages.error(context.getSource(), "stellarsweeper.list.not_found", listName);
            return 0;
        }
        Messages.success(context.getSource(), "stellarsweeper.list.switched", false, listName);
        return 1;
    }

    public static int addHeldItem(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if (!(context.getSource().getEntity() instanceof ServerPlayer player)) {
            throw PLAYER_ONLY.create();
        }
        ItemStack held = player.getMainHandItem();
        if (held.isEmpty()) {
            Messages.error(context.getSource(), "stellarsweeper.no_item_held");
            return 0;
        }
        String itemId = BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        ConfigManager.AddItemResult result = StellarSweeper.configManager().addItemToActiveList(itemId);
        if (result == ConfigManager.AddItemResult.ALREADY_EXISTS) {
            Messages.error(context.getSource(), "stellarsweeper.item.already_exists", itemId);
            return 0;
        }
        Messages.success(context.getSource(), "stellarsweeper.item.added", false, itemId);
        return 1;
    }

    public static int addItemById(CommandContext<CommandSourceStack> context, String rawItemId) {
        String itemId = rawItemId.toLowerCase(Locale.ROOT);
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
            Messages.error(context.getSource(), "stellarsweeper.item.invalid", itemId);
            return 0;
        }
        ConfigManager.AddItemResult result = StellarSweeper.configManager().addItemToActiveList(itemId);
        if (result == ConfigManager.AddItemResult.ALREADY_EXISTS) {
            Messages.error(context.getSource(), "stellarsweeper.item.already_exists", itemId);
            return 0;
        }
        Messages.success(context.getSource(), "stellarsweeper.item.added", false, itemId);
        return 1;
    }

    public static int removeItemById(CommandContext<CommandSourceStack> context, String itemId) {
        boolean removed = StellarSweeper.configManager().removeItemFromActiveList(itemId);
        if (!removed) {
            Messages.error(context.getSource(), "stellarsweeper.item.not_found", itemId);
            return 0;
        }
        Messages.success(context.getSource(), "stellarsweeper.item.removed", false, itemId);
        return 1;
    }

    public static int confirm(CommandContext<CommandSourceStack> context, boolean yes) {
        long requestId = com.mojang.brigadier.arguments.LongArgumentType.getLong(context, "requestId");
        ThresholdPromptManager.ConfirmationResult result = StellarSweeper.thresholdPromptManager().handleConfirmation(
                context.getSource(),
                requestId,
                yes,
                StellarSweeper.cleanupService()
        );
        if (result.status() == ThresholdPromptManager.ConfirmationStatus.EXPIRED) {
            Messages.error(context.getSource(), "stellarsweeper.confirm.expired");
            return 0;
        }
        if (result.status() == ThresholdPromptManager.ConfirmationStatus.NOT_FOUND) {
            Messages.error(context.getSource(), "stellarsweeper.confirm.expired");
            return 0;
        }
        if (result.status() == ThresholdPromptManager.ConfirmationStatus.DECLINED) {
            Messages.warning(context.getSource(), "stellarsweeper.confirm.no", true);
            return 1;
        }
        Messages.success(context.getSource(), "stellarsweeper.confirm.yes", true);
        Messages.broadcastCleanupReport(context.getSource().getServer(), result.report(), false);
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestListNames(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Collection<String> listNames = StellarSweeper.configManager().get().cleanupLists.keySet();
        return SharedSuggestionProvider.suggest(listNames, builder);
    }

    public static CompletableFuture<Suggestions> suggestActiveListItems(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> itemIds = StellarSweeper.configManager().get().getActiveCleanupList();
        return SharedSuggestionProvider.suggest(itemIds, builder);
    }

    public static CompletableFuture<Suggestions> suggestAllItems(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(
                BuiltInRegistries.ITEM.keySet().stream().map(ResourceLocation::toString),
                builder
        );
    }

    private static void sendCommandLine(CommandSourceStack source, String command, String descriptionKey, Object... descriptionArgs) {
        source.sendSuccess(() -> Messages.commandLine(command, descriptionKey, descriptionArgs), false);
    }

    private static int listNamedInternal(CommandSourceStack source, String listName, List<String> items) {
        source.sendSuccess(() -> Messages.info(
                "stellarsweeper.list.title",
                Component.literal(listName).withStyle(ChatFormatting.GREEN)
        ), false);
        source.sendSuccess(Messages::divider, false);
        if (items.isEmpty()) {
            Messages.warning(source, "stellarsweeper.list.empty", false);
            return 1;
        }
        for (String itemId : items) {
            source.sendSuccess(() -> Messages.listItemEntry(itemId), false);
        }
        return 1;
    }
}
