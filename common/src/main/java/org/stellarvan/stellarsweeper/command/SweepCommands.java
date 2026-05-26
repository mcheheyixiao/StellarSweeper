package org.stellarvan.stellarsweeper.command;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;

public final class SweepCommands {
    private SweepCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("sweep")
                .requires(source -> source.hasPermission(2))
                .executes(SweepCommandHandlers::showShortHelp)
                .then(literal("help").executes(SweepCommandHandlers::showHelp))
                .then(literal("run").executes(SweepCommandHandlers::runSweep))
                .then(literal("preview").executes(SweepCommandHandlers::previewSweep))
                .then(literal("reload").executes(SweepCommandHandlers::reloadConfig))
                .then(literal("save").executes(SweepCommandHandlers::saveConfig))
                .then(literal("toggle")
                        .then(literal("auto").executes(SweepCommandHandlers::toggleAuto))
                        .then(literal("threshold").executes(SweepCommandHandlers::toggleThreshold)))
                .then(literal("set")
                        .then(literal("interval")
                                .then(argument("ticks", IntegerArgumentType.integer(200))
                                        .executes(context -> SweepCommandHandlers.setInterval(
                                                context,
                                                IntegerArgumentType.getInteger(context, "ticks")
                                        ))))
                        .then(literal("threshold")
                                .then(argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> SweepCommandHandlers.setThreshold(
                                                context,
                                                IntegerArgumentType.getInteger(context, "count")
                                        ))))
                        .then(literal("radius")
                                .then(argument("blocks", IntegerArgumentType.integer(1))
                                        .executes(context -> SweepCommandHandlers.setRadius(
                                                context,
                                                IntegerArgumentType.getInteger(context, "blocks")
                                        ))))
                        .then(literal("y")
                                .then(argument("min", IntegerArgumentType.integer())
                                        .then(argument("max", IntegerArgumentType.integer())
                                                .executes(context -> SweepCommandHandlers.setYRange(
                                                        context,
                                                        IntegerArgumentType.getInteger(context, "min"),
                                                        IntegerArgumentType.getInteger(context, "max")
                                                ))))))
                .then(literal("list")
                        .executes(SweepCommandHandlers::listCurrent)
                        .then(argument("listName", StringArgumentType.word())
                                .suggests(SweepCommandHandlers::suggestListNames)
                                .executes(context -> SweepCommandHandlers.listNamed(
                                        context,
                                        StringArgumentType.getString(context, "listName")
                                ))))
                .then(literal("lists").executes(SweepCommandHandlers::listAll))
                .then(literal("list-create")
                        .then(argument("listName", StringArgumentType.word())
                                .executes(context -> SweepCommandHandlers.createList(
                                        context,
                                        StringArgumentType.getString(context, "listName")
                                ))))
                .then(literal("list-delete")
                        .then(argument("listName", StringArgumentType.word())
                                .suggests(SweepCommandHandlers::suggestListNames)
                                .executes(context -> SweepCommandHandlers.deleteList(
                                        context,
                                        StringArgumentType.getString(context, "listName")
                                ))))
                .then(literal("list-use")
                        .then(argument("listName", StringArgumentType.word())
                                .suggests(SweepCommandHandlers::suggestListNames)
                                .executes(context -> SweepCommandHandlers.useList(
                                        context,
                                        StringArgumentType.getString(context, "listName")
                                ))))
                .then(literal("add")
                        .executes(SweepCommandHandlers::addHeldItem)
                        .then(argument("itemId", StringArgumentType.word())
                                .suggests(SweepCommandHandlers::suggestAllItems)
                                .executes(context -> SweepCommandHandlers.addItemById(
                                        context,
                                        StringArgumentType.getString(context, "itemId")
                                ))))
                .then(literal("remove")
                        .then(argument("itemId", StringArgumentType.word())
                                .suggests(SweepCommandHandlers::suggestActiveListItems)
                                .executes(context -> SweepCommandHandlers.removeItemById(
                                        context,
                                        StringArgumentType.getString(context, "itemId")
                                ))))
                .then(literal("confirm")
                        .then(argument("requestId", LongArgumentType.longArg(1))
                                .then(literal("yes").executes(context -> SweepCommandHandlers.confirm(context, true)))
                                .then(literal("no").executes(context -> SweepCommandHandlers.confirm(context, false))))));
    }
}
