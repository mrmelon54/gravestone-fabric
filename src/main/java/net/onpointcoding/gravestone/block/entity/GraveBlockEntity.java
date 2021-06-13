package net.onpointcoding.gravestone.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.onpointcoding.gravestone.Gravestone;

import java.util.List;
import java.util.UUID;

public class GraveBlockEntity extends BlockEntity implements Inventory {
    private DefaultedList<ItemStack> inventory;
    private int storedExperience;
    private UUID graveOwner;

    public GraveBlockEntity(BlockPos pos, BlockState state) {
        super(Gravestone.GRAVE_BLOCK_ENTITY, pos, state);
        inventory = DefaultedList.ofSize(41, ItemStack.EMPTY);
        storedExperience = 0;
        graveOwner = null;
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, inventory);

        if (nbt.contains("GraveOwner")) graveOwner = nbt.getUuid("GraveOwner");
        else graveOwner = null;

        if (nbt.contains("StoredExperience")) storedExperience = nbt.getInt("StoredExperience");
        else storedExperience = 0;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);

        nbt.putInt("StoredExperience", storedExperience);
        nbt.putUuid("GraveOwner", graveOwner);

        return nbt;
    }

    @Override
    public int size() {
        return 41;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getStack(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return null;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.removeStack(inventory, slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return false;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    public List<ItemStack> getInventory() {
        return inventory;
    }

    public int getStoredExperience() {
        return storedExperience;
    }

    public void setStoredExperience(int v) {
        storedExperience = v;
    }

    public UUID getGraveOwner() {
        return graveOwner;
    }

    public void setGraveOwner(UUID uuid) {
        graveOwner = uuid;
    }
}
