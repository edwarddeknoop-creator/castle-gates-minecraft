package com.castlegates.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.Direction;

/**
 * Ett vindbryggsblock. Bygg bron stående (uppfälld) i portöppningen.
 * FACING = åt vilket håll bron fälls ner. Det blir det håll du tittar åt
 * när du placerar blocket, så stå innanför borgen och titta ut mot vallgraven.
 */
public class DrawbridgeBlock extends Block {
	public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

	public DrawbridgeBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
	}
}
