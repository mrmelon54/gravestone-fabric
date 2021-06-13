package net.onpointcoding.gravestone.item;

import net.fabricmc.fabric.api.util.NbtType;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ObituaryPaperItem extends WrittenBookItem implements ItemConvertible {
    public ObituaryPaperItem(Settings settings) {
        super(settings);
    }

    public static boolean isValid(@Nullable NbtCompound nbt) {
        if (!WritableBookItem.isValid(nbt)) {
            return false;
        } else if (!nbt.contains("title", NbtType.STRING)) {
            return false;
        } else {
            String string = nbt.getString("title");
            return string.length() <= 32;
        }
    }

    @Override
    public Text getName(ItemStack stack) {
        NbtCompound nbtCompound = stack.getTag();
        if (nbtCompound != null) {
            String string = nbtCompound.getString("title");
            if (!ChatUtil.isEmpty(string)) {
                return new LiteralText(string);
            }
        }

        return super.getName(stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        user.useBook(itemStack, hand);
        return TypedActionResult.success(itemStack, world.isClient());
    }

    public boolean hasGlint(ItemStack stack) {
        return false;
    }
}
