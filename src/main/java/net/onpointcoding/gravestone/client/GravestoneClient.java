package net.onpointcoding.gravestone.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.onpointcoding.gravestone.Gravestone;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class GravestoneClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(Gravestone.GRAVE_BLOCK, RenderLayer.getTranslucent());
    }
}
