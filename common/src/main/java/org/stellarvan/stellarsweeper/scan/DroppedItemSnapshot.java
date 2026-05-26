package org.stellarvan.stellarsweeper.scan;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class DroppedItemSnapshot {
    private final UUID entityUuid;
    private final ServerLevel level;
    private final ItemEntity entity;
    private final ItemStack stack;
    private final String itemId;
    private final String displayName;

    public DroppedItemSnapshot(
            UUID entityUuid,
            ServerLevel level,
            ItemEntity entity,
            ItemStack stack,
            String itemId,
            String displayName
    ) {
        this.entityUuid = entityUuid;
        this.level = level;
        this.entity = entity;
        this.stack = stack;
        this.itemId = itemId;
        this.displayName = displayName;
    }

    public UUID entityUuid() {
        return entityUuid;
    }

    public ServerLevel level() {
        return level;
    }

    public ItemEntity entity() {
        return entity;
    }

    public ItemStack stack() {
        return stack;
    }

    public String itemId() {
        return itemId;
    }

    public String displayName() {
        return displayName;
    }
}
