package net.onpointcoding.gravestone;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.onpointcoding.gravestone.block.GraveBlock;
import net.onpointcoding.gravestone.block.entity.GraveBlockEntity;
import net.onpointcoding.gravestone.duck.IObituaryHolder;
import net.onpointcoding.gravestone.item.ObituaryPaperItem;

import java.util.*;
import java.util.stream.Stream;

public class Gravestone implements ModInitializer {
    public static final Block GRAVE_BLOCK = new GraveBlock(FabricBlockSettings.of(Material.STONE).collidable(false).breakByHand(true).strength(-1f, 3600000f).luminance(15).nonOpaque());
    public static final ItemConvertible OBITUARY_PAPER = new ObituaryPaperItem(new FabricItemSettings().maxCount(1));
    public static BlockEntityType<GraveBlockEntity> GRAVE_BLOCK_ENTITY;

    public static void placeGrave(World world, Vec3d pos, PlayerEntity player) {
        if (world.isClient) return;

        Direction playerFacing = player.getHorizontalFacing();

        BlockPos blockPos = new BlockPos(pos.x, pos.y - 1, pos.z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();

        if (blockPos.getY() < 0) {
            blockPos = new BlockPos(blockPos.getX(), 10, blockPos.getZ());
        }

        for (BlockPos gravePos : BlockPos.iterateOutwards(blockPos.add(new Vec3i(0, 1, 0)), 5, 5, 5)) {
            if (canPlaceGravestone(world, block, gravePos)) {
                Random rand = new Random();
                int graveColorIndex = rand.nextInt(16);

                GraveBlockEntity gravestoneBlockEntity = new GraveBlockEntity(gravePos, GRAVE_BLOCK.getDefaultState());
                world.setBlockState(gravePos, GRAVE_BLOCK.getDefaultState().with(Properties.HORIZONTAL_FACING, playerFacing).with(GraveBlock.COLOR_PROPERTY, graveColorIndex));
                world.addBlockEntity(gravestoneBlockEntity);

                DefaultedList<ItemStack> main = player.getInventory().main;
                DefaultedList<ItemStack> armor = player.getInventory().armor;
                DefaultedList<ItemStack> offHand = player.getInventory().offHand;

                for (int i = 0; i < main.size(); i++) gravestoneBlockEntity.setStack(i, main.get(i));
                for (int i = 0; i < armor.size(); i++) gravestoneBlockEntity.setStack(i + 36, armor.get(i));
                for (int i = 0; i < offHand.size(); i++) gravestoneBlockEntity.setStack(i + 40, offHand.get(i));
                main.clear();
                armor.clear();
                offHand.clear();

                gravestoneBlockEntity.setStoredExperience((int) Math.floor(player.totalExperience * 0.8f));
                gravestoneBlockEntity.setGraveOwner(player.getUuid());

                block.onBreak(world, blockPos, blockState, player);

                if (player instanceof IObituaryHolder obituaryHoldingPlayer) {
                    ItemStack itemStack = new ItemStack(Gravestone.OBITUARY_PAPER);
                    NbtCompound bookStack = itemStack.getOrCreateTag();
                    bookStack.putString("title", player.getName().asString() + "'s Obituary");

                    List<Text> bookText = new ArrayList<>();
                    bookText.add(new TranslatableText("text.gravestone.obituary.page1", gravePos.getX(), gravePos.getY(), gravePos.getZ()));
                    Stream<NbtString> nbtStringStream = bookText.stream().map(text -> NbtString.of(Text.Serializer.toJson(text)));

                    NbtList pageData = new NbtList();
                    nbtStringStream.forEach(pageData::add);
                    bookStack.put("pages", pageData);

                    player.getInventory().clear();
                    obituaryHoldingPlayer.setObituary(itemStack);
                }

                System.out.printf("[Gravestone] Grave spawned at: %d, %d, %d for %s%n", gravePos.getX(), gravePos.getY(), gravePos.getZ(), player.getDisplayName().asString());

                break;
            }
        }
    }

    private static boolean canPlaceGravestone(World world, Block block, BlockPos blockPos) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);

        if (blockEntity != null) return false;

        Set<Block> blackListedBlocks = new HashSet<>() {{
            add(Blocks.BEDROCK);
        }};

        if (blackListedBlocks.contains(block)) return false;

        return !(blockPos.getY() < 0 || blockPos.getY() > 255);
    }

    @Override
    public void onInitialize() {
        Registry.register(Registry.BLOCK, new Identifier("gravestone", "grave"), GRAVE_BLOCK);
        GRAVE_BLOCK_ENTITY = Registry.register(Registry.BLOCK_ENTITY_TYPE, "gravestone:grave", FabricBlockEntityTypeBuilder.create(GraveBlockEntity::new, GRAVE_BLOCK).build(null));

        // -- Don't need the gravestone block item as it can only be generated by dying
        //Registry.register(Registry.ITEM, new Identifier("gravestone", "grave"), new BlockItem(GRAVE_BLOCK, new FabricItemSettings().group(ItemGroup.MISC)));

        Registry.register(Registry.ITEM, new Identifier("gravestone", "obituary"), OBITUARY_PAPER.asItem());

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            if (oldPlayer instanceof IObituaryHolder oldObituaryHoldingPlayer) {
                ItemStack obituary = oldObituaryHoldingPlayer.getObituary();
                newPlayer.getInventory().insertStack(obituary);
            }
        });
    }
}
