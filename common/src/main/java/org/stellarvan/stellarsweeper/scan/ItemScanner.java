package org.stellarvan.stellarsweeper.scan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

public final class ItemScanner {
    public ScanResult scan(MinecraftServer server, ScanScope scope) {
        Set<UUID> seen = new HashSet<>();
        List<DroppedItemSnapshot> snapshots = new ArrayList<>();
        boolean hasPlayers = false;

        for (ServerLevel level : server.getAllLevels()) {
            List<ServerPlayer> players = level.players();
            if (players.isEmpty()) {
                continue;
            }
            hasPlayers = true;
            for (ServerPlayer player : players) {
                AABB box = createPlayerScanBox(player, scope);
                List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, box, ItemEntity::isAlive);
                for (ItemEntity item : items) {
                    if (!seen.add(item.getUUID())) {
                        continue;
                    }
                    ItemStack stack = item.getItem();
                    String itemId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                    snapshots.add(new DroppedItemSnapshot(
                            item.getUUID(),
                            level,
                            item,
                            stack.copy(),
                            itemId,
                            stack.getHoverName().getString()
                    ));
                }
            }
        }
        return new ScanResult(snapshots, hasPlayers);
    }

    private AABB createPlayerScanBox(ServerPlayer player, ScanScope scope) {
        double x = player.getX();
        double z = player.getZ();
        int radius = scope.cleanRadius();
        return new AABB(
                x - radius,
                scope.yMin(),
                z - radius,
                x + radius,
                scope.yMax(),
                z + radius
        );
    }
}
