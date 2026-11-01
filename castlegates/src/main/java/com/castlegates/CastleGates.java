package com.castlegates;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CastleGates implements ModInitializer {
	public static final String MOD_ID = "castlegates";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.init();
		LOGGER.info("Castle Gates loaded – raise the portcullis!");
	}
}
