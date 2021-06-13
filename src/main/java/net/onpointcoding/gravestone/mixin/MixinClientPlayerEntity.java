package net.onpointcoding.gravestone.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.onpointcoding.gravestone.Gravestone;
import net.onpointcoding.gravestone.client.screen.ObituaryPaperScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity {
    @Shadow
    @Final
    protected MinecraftClient client;

    @Inject(method = "useBook", at = @At("TAIL"))
    public void onOpenWrittenBook(ItemStack book, Hand hand, CallbackInfo ci) {
        if (client.player != null) {
            ItemStack itemStack = client.player.getStackInHand(hand);
            if (itemStack.isOf(Gravestone.OBITUARY_PAPER.asItem()))
                client.openScreen(new ObituaryPaperScreen(new ObituaryPaperScreen.ObituaryPaperContents(itemStack)));
        }
    }
}
