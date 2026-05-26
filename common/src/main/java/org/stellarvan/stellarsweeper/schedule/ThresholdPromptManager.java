package org.stellarvan.stellarsweeper.schedule;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.stellarvan.stellarsweeper.Constants;
import org.stellarvan.stellarsweeper.cleanup.CleanupCause;
import org.stellarvan.stellarsweeper.cleanup.CleanupReport;
import org.stellarvan.stellarsweeper.cleanup.CleanupRequest;
import org.stellarvan.stellarsweeper.cleanup.CleanupService;
import org.stellarvan.stellarsweeper.text.Messages;

public final class ThresholdPromptManager {
    private final Map<Long, CleanupRequest> pendingRequests = new LinkedHashMap<>();
    private long nextRequestId = 1L;
    private long lastPromptTick = -1L;

    public boolean canPrompt(long nowTick, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return true;
        }
        if (lastPromptTick < 0) {
            return true;
        }
        return nowTick - lastPromptTick >= cooldownTicks;
    }

    public void sendThresholdPrompt(MinecraftServer server, int totalCount, long nowTick, long expireTick) {
        cleanupExpired(nowTick);
        if (!hasOpPlayers(server)) {
            Constants.LOGGER.info("Threshold reached ({}), but no OP2 online. Prompt skipped.", totalCount);
            return;
        }

        long requestId = nextRequestId++;
        pendingRequests.put(requestId, new CleanupRequest(requestId, nowTick, expireTick));
        lastPromptTick = nowTick;

        MutableComponent prompt = Messages.prefixed("stellarsweeper.threshold.prompt", totalCount);
        MutableComponent yesButton = Component.translatable("stellarsweeper.button.yes")
                .withStyle(style -> style
                        .withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/sweep confirm " + requestId + " yes"
                        )));
        MutableComponent noButton = Component.translatable("stellarsweeper.button.no")
                .withStyle(style -> style
                        .withColor(ChatFormatting.RED)
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.RUN_COMMAND,
                                "/sweep confirm " + requestId + " no"
                        )));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasPermissions(2)) {
                player.sendSystemMessage(prompt.copy().append(Component.literal(" ")).append(yesButton).append(Component.literal(" ")).append(noButton));
            } else {
                player.sendSystemMessage(prompt);
            }
        }
    }

    public ConfirmationResult handleConfirmation(
            CommandSourceStack source,
            long requestId,
            boolean yes,
            CleanupService cleanupService
    ) {
        MinecraftServer server = source.getServer();
        long nowTick = server.getTickCount();
        cleanupExpired(nowTick);
        CleanupRequest request = pendingRequests.remove(requestId);
        if (request == null) {
            return new ConfirmationResult(ConfirmationStatus.NOT_FOUND, null);
        }
        if (request.expired(nowTick)) {
            return new ConfirmationResult(ConfirmationStatus.EXPIRED, null);
        }
        if (!yes) {
            return new ConfirmationResult(ConfirmationStatus.DECLINED, null);
        }
        CleanupReport report = cleanupService.sweep(server, CleanupCause.THRESHOLD_CONFIRM, true);
        return new ConfirmationResult(ConfirmationStatus.EXECUTED, report);
    }

    public void cleanupExpired(long nowTick) {
        Iterator<Map.Entry<Long, CleanupRequest>> iterator = pendingRequests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, CleanupRequest> entry = iterator.next();
            if (entry.getValue().expired(nowTick)) {
                iterator.remove();
            }
        }
    }

    private boolean hasOpPlayers(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.hasPermissions(2)) {
                return true;
            }
        }
        return false;
    }

    public enum ConfirmationStatus {
        EXECUTED,
        DECLINED,
        EXPIRED,
        NOT_FOUND
    }

    public record ConfirmationResult(ConfirmationStatus status, CleanupReport report) {
    }
}
