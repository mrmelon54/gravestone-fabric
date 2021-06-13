package net.onpointcoding.gravestone.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import net.onpointcoding.gravestone.Gravestone;
import net.onpointcoding.gravestone.duck.IObituaryHolder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IObituaryHolder {
    @Shadow
    @Final
    private PlayerInventory inventory;

    private ItemStack obituary;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        obituary = ItemStack.EMPTY;
    }

    @Inject(method = "dropInventory", at = @At(value = "INVOKE", target = "net.minecraft.entity.player.PlayerInventory.dropAll()V"), cancellable = true)
    public void dropInventory(CallbackInfo ci) {
        Gravestone.placeGrave(world, getPos(), inventory.player);

        ci.cancel();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("RETURN"))
    public void writeNbt(NbtCompound nbt, CallbackInfo ci) {
        NbtCompound obituaryTag = new NbtCompound();
        if (obituary != null) obituary.writeNbt(obituaryTag);
        nbt.put("Obituary", obituaryTag);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("RETURN"))
    public void readNbt(NbtCompound nbt, CallbackInfo ci) {
        if (nbt.contains("Obituary")) {
            obituary = ItemStack.fromNbt(nbt.getCompound("Obituary"));
            if (obituary == null) obituary = ItemStack.EMPTY;
        }
    }


    @Override
    public void setObituary(ItemStack stack) {
        obituary = stack.copy();
    }

    @Override
    public ItemStack getObituary() {
        return obituary;
    }
}
