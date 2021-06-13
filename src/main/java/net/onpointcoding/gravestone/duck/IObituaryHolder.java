package net.onpointcoding.gravestone.duck;

import net.minecraft.item.ItemStack;

public interface IObituaryHolder {
    void setObituary(ItemStack stack);

    ItemStack getObituary();
}
