package net.onpointcoding.gravestone.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.onpointcoding.gravestone.block.entity.GraveBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class GraveBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final IntProperty COLOR_PROPERTY = IntProperty.of("color", 0, 15);

    public GraveBlock(Settings settings) {
        super(settings);
        setDefaultState(this.stateManager.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH).with(COLOR_PROPERTY, 0));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> stateManager) {
        stateManager.add(Properties.HORIZONTAL_FACING).add(COLOR_PROPERTY);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GraveBlockEntity(pos, state);
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        onBreak(world, pos, state, player);
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        retrieveGraveInventory(world, pos, player);
    }

    public void retrieveGraveInventory(World world, BlockPos pos, PlayerEntity player) {
        if (world.isClient) return;

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof GraveBlockEntity graveBlockEntity)) return;

        UUID graveOwner = graveBlockEntity.getGraveOwner();
        if (graveOwner != null && graveOwner.compareTo(player.getUuid()) != 0) return;

        // Get player inventories
        DefaultedList<ItemStack> player_main = player.getInventory().main;
        DefaultedList<ItemStack> player_armor = player.getInventory().armor;
        DefaultedList<ItemStack> player_offHand = player.getInventory().offHand;

        // Add player inventories to lists
        DefaultedList<ItemStack> old_player_inventory = DefaultedList.ofSize(player_main.size(), ItemStack.EMPTY);
        for (int i = 0; i < player_main.size(); i++) old_player_inventory.set(i, player_main.get(i));

        List<ItemStack> old_player_offHand = DefaultedList.ofSize(player_offHand.size(), ItemStack.EMPTY);
        for (int i = 0; i < player_offHand.size(); i++) old_player_offHand.set(i, player_offHand.get(i));

        DefaultedList<ItemStack> old_player_armor = DefaultedList.ofSize(player_armor.size(), ItemStack.EMPTY);
        for (int i = 0; i < player_armor.size(); i++) old_player_armor.set(i, player_armor.get(i));

        // Get items from grave block entity
        List<ItemStack> main = graveBlockEntity.getInventory().subList(0, 36);
        List<ItemStack> armor = graveBlockEntity.getInventory().subList(36, 40);
        List<ItemStack> offHand = graveBlockEntity.getInventory().subList(40, 41);

        // Put items from grave into player
        for (int i = 0; i < main.size(); i++) player_main.set(i, main.get(i));
        for (int i = 0; i < armor.size(); i++) player_armor.set(i, armor.get(i));
        for (int i = 0; i < offHand.size(); i++) player.getInventory().offHand.set(i, offHand.get(i));

        // Check if item has space in original slot then put it in
        for (int i = 0; i < old_player_armor.size(); i++) {
            if (player_armor.get(i).isEmpty()) {
                player_armor.set(i, old_player_armor.get(i));
                old_player_armor.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < old_player_inventory.size() - 1; i++) {
            if (player_main.get(i).isEmpty()) {
                player_main.set(i, old_player_inventory.get(i));
                old_player_inventory.set(i, ItemStack.EMPTY);
            }
        }
        for (int i = 0; i < old_player_offHand.size(); i++) {
            if (player_offHand.get(i).isEmpty()) {
                player_offHand.set(i, old_player_offHand.get(i));
                old_player_offHand.set(i, ItemStack.EMPTY);
            }
        }

        // Get remainder list if items couldn't be put back into the player inventory
        DefaultedList<ItemStack> remainder = DefaultedList.ofSize(old_player_inventory.size() + old_player_armor.size() + old_player_offHand.size(), ItemStack.EMPTY);

        // Copy all the items from the old player storage into the remainder variable
        for (int i = 0; i < old_player_inventory.size(); i++)
            remainder.set(i, old_player_inventory.get(i));
        for (int i = 0; i < old_player_armor.size(); i++)
            remainder.set(i + old_player_inventory.size(), old_player_armor.get(i));
        for (int i = 0; i < old_player_offHand.size(); i++)
            remainder.set(i + old_player_inventory.size() + old_player_armor.size(), old_player_offHand.get(i));

        for (int i = 0; i < remainder.size(); i++) {
            // Try and put the items in a stack
            if (remainder.get(i).isEmpty()) continue;
            if (!player.getInventory().insertStack(remainder.get(i))) break;
            remainder.set(i, ItemStack.EMPTY);
        }

        // Drop remaining items
        for (ItemStack itemStack : remainder) player.dropStack(itemStack);

        player.addExperience(graveBlockEntity.getStoredExperience());
        world.removeBlock(pos, false);
        world.removeBlockEntity(pos);
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }
}
