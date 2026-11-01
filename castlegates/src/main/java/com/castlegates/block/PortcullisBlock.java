package com.castlegates.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/**
 * Ett fällgaller: en tunn järnpanel. Bygg en stor vägg av dem i portöppningen
 * och häng en vinsch ovanför med kedjor ner till gallrets översta rad.
 */
public class PortcullisBlock extends Block {
	public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

	private static final VoxelShape X_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 16, 10);
	private static final VoxelShape Z_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 16, 16);

	public PortcullisBlock(Settings settings) {
		super(settings);
		setDefaultState(getDefaultState().with(AXIS, Direction.Axis.X));
	}

	@Override
	protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
		builder.add(AXIS);
	}

	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		// Panelen läggs på tvären mot spelaren, så man ser gallret framifrån.
		Direction facing = ctx.getHorizontalPlayerFacing();
		return getDefaultState().with(AXIS, facing.rotateYClockwise().getAxis());
	}

	@Override
	protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return state.get(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
	}
}
