package com.castlegates;

import com.castlegates.block.DrawbridgeBlock;
import com.castlegates.block.PortcullisBlock;
import com.castlegates.block.WinchBlock;
import com.castlegates.block.WinchBlockEntity;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class ModBlocks {
	private ModBlocks() {}

	/** Fällgaller – järngaller som dras rakt upp. */
	public static final Block PORTCULLIS = register("portcullis", new PortcullisBlock(AbstractBlock.Settings.create()
			.mapColor(MapColor.IRON_GRAY)
			.strength(5.0f, 6.0f)
			.requiresTool()
			.sounds(BlockSoundGroup.CHAIN)
			.nonOpaque()
			.solidBlock((state, world, pos) -> false)
			.suffocates((state, world, pos) -> false)
			.blockVision((state, world, pos) -> false)));

	/** Vindbrygga – träbro som fälls ner och dras upp. */
	public static final Block DRAWBRIDGE = register("drawbridge", new DrawbridgeBlock(AbstractBlock.Settings.create()
			.mapColor(MapColor.SPRUCE_BROWN)
			.strength(2.5f, 3.0f)
			.sounds(BlockSoundGroup.WOOD)
			.burnable()));

	/** Vinsch – drar i kedjorna. */
	public static final Block WINCH = register("winch", new WinchBlock(AbstractBlock.Settings.create()
			.mapColor(MapColor.IRON_GRAY)
			.strength(3.5f, 6.0f)
			.requiresTool()
			.sounds(BlockSoundGroup.METAL)));

	public static final BlockEntityType<WinchBlockEntity> WINCH_BLOCK_ENTITY = Registry.register(
			Registries.BLOCK_ENTITY_TYPE,
			Identifier.of(CastleGates.MOD_ID, "winch"),
			BlockEntityType.Builder.create(WinchBlockEntity::new, WINCH).build(null));

	private static Block register(String name, Block block) {
		Identifier id = Identifier.of(CastleGates.MOD_ID, name);
		Registry.register(Registries.BLOCK, id, block);
		Registry.register(Registries.ITEM, id, new BlockItem(block, new Item.Settings()));
		return block;
	}

	public static void init() {
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(entries -> {
			entries.add(WINCH);
			entries.add(PORTCULLIS);
			entries.add(DRAWBRIDGE);
		});
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.BUILDING_BLOCKS).register(entries -> {
			entries.add(PORTCULLIS);
			entries.add(DRAWBRIDGE);
		});
	}
}
