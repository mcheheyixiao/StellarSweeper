package org.stellarvan.stellarsweeper.rule;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

public final class CleanupContext {
    private final ItemEntity entity;
    private final ItemStack stack;
    private final String itemId;

    public CleanupContext(ItemEntity entity, ItemStack stack, String itemId) {
        this.entity = entity;
        this.stack = stack;
        this.itemId = itemId;
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
}
