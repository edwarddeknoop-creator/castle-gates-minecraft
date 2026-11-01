package com.castlegates.client;

import com.castlegates.ModBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public class CastleGatesClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Gallret har genomskinliga hål.
		BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.PORTCULLIS, RenderLayer.getCutout());
	}
}
