package com.castlegates.block;

import com.castlegates.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hjärnan i vinschen. Håller reda på vilken grind den är kopplad till och
 * flyttar grinden ett steg i taget så att det ser ut som en animation.
 *
 * Fällgaller: hela gallret flyttas upp ett block per steg. Där det finns en
 * fast vägg ovanför "försvinner" gallret in i väggen; där det är luft syns det.
 * Kedjorna under vinschen lindas upp när gallret stiger och rullas ut igen
 * när det sänks.
 *
 * Vindbrygga: bron roteras kring sin nedersta rad i tre steg (30°, 60°, 90°)
 * tills den ligger platt. Kedjorna släpps ut medan bron är nere.
 */
public class WinchBlockEntity extends BlockEntity {
	public enum GateType { PORTCULLIS, DRAWBRIDGE }

	private static final int MAX_GATE_BLOCKS = 1024;
	private static final int MAX_GATE_HEIGHT = 48;
	private static final int MAX_CHAIN_LENGTH = 64;
	private static final int DRAWBRIDGE_FRAMES = 3;
	private static final int PORTCULLIS_TICKS_PER_STEP = 6;
	private static final int DRAWBRIDGE_TICKS_PER_STEP = 12;

	private boolean open;
	private int step;
	private int cooldown;

	// Kopplingsdata – bara satt medan grinden är (helt eller delvis) öppen.
	private GateType type;
	private List<BlockPos> gate = new ArrayList<>();
	private List<BlockPos> chains = new ArrayList<>();
	private BlockState gateState;
	private int maxSteps;
	private int hingeY;
	private int hingeOffset;
	private Direction outward = Direction.NORTH;

	public WinchBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlocks.WINCH_BLOCK_ENTITY, pos, state);
	}

	// ------------------------------------------------------------------
	// Styrning
	// ------------------------------------------------------------------

	public Text toggle() {
		return setOpen(!open);
	}

	public Text setOpen(boolean wantOpen) {
		if (world == null) {
			return Text.empty();
		}
		if (wantOpen == open) {
			return Text.translatable(open ? "message.castlegates.already_open" : "message.castlegates.already_closed");
		}
		if (wantOpen && type == null) {
			Text error = scan();
			if (error != null) {
				return error;
			}
		}
		open = wantOpen;
		cooldown = 0;
		markDirty();
		return Text.translatable(open ? "message.castlegates.opening" : "message.castlegates.closing");
	}

	/** Letar upp kedjorna och grinden under vinschen. Returnerar ett felmeddelande, eller null om allt gick bra. */
	private Text scan() {
		World w = world;
		List<BlockPos> chainList = new ArrayList<>();
		BlockPos p = pos.down();
		while (w.getBlockState(p).isOf(Blocks.CHAIN) && chainList.size() < MAX_CHAIN_LENGTH) {
			chainList.add(p);
			p = p.down();
		}

		BlockState start = w.getBlockState(p);
		GateType foundType;
		if (start.isOf(ModBlocks.PORTCULLIS)) {
			foundType = GateType.PORTCULLIS;
		} else if (start.isOf(ModBlocks.DRAWBRIDGE)) {
			foundType = GateType.DRAWBRIDGE;
		} else {
			return Text.translatable("message.castlegates.no_gate");
		}

		// Hitta alla sammanhängande grindblock (flood fill).
		Block gateBlock = start.getBlock();
		Set<BlockPos> found = new HashSet<>();
		ArrayDeque<BlockPos> queue = new ArrayDeque<>();
		found.add(p);
		queue.add(p);
		int minY = p.getY();
		int maxY = p.getY();
		while (!queue.isEmpty()) {
			BlockPos current = queue.poll();
			for (Direction d : Direction.values()) {
				BlockPos next = current.offset(d);
				if (next.getY() >= pos.getY() || found.contains(next)) {
					continue;
				}
				if (w.getBlockState(next).isOf(gateBlock)) {
					if (found.size() >= MAX_GATE_BLOCKS) {
						return Text.translatable("message.castlegates.too_big", MAX_GATE_BLOCKS);
					}
					found.add(next);
					queue.add(next);
					minY = Math.min(minY, next.getY());
					maxY = Math.max(maxY, next.getY());
				}
			}
		}
		int height = maxY - minY + 1;
		if (height > MAX_GATE_HEIGHT) {
			return Text.translatable("message.castlegates.too_big", MAX_GATE_BLOCKS);
		}

		type = foundType;
		gate = new ArrayList<>(found);
		chains = chainList;
		gateState = start;
		step = 0;
		if (foundType == GateType.PORTCULLIS) {
			maxSteps = height;
		} else {
			maxSteps = DRAWBRIDGE_FRAMES;
			outward = start.get(DrawbridgeBlock.FACING);
			hingeY = minY;
			hingeOffset = along(p, outward);
		}
		return null;
	}

	private void unlink() {
		type = null;
		gate = new ArrayList<>();
		chains = new ArrayList<>();
		gateState = null;
		maxSteps = 0;
		step = 0;
		markDirty();
	}

	// ------------------------------------------------------------------
	// Tick / animation
	// ------------------------------------------------------------------

	public static void tick(World world, BlockPos pos, BlockState state, WinchBlockEntity be) {
		if (be.type == null) {
			return;
		}
		int target = be.open ? be.maxSteps : 0;
		if (be.step == target) {
			if (!be.open) {
				be.unlink();
			}
			return;
		}
		int ticksPerStep = be.type == GateType.PORTCULLIS ? PORTCULLIS_TICKS_PER_STEP : DRAWBRIDGE_TICKS_PER_STEP;
		if (++be.cooldown < ticksPerStep) {
			return;
		}
		be.cooldown = 0;

		int next = be.step + (target > be.step ? 1 : -1);
		be.move(be.step, next);
		be.step = next;
		be.markDirty();
		be.playStepSounds(next == target);
	}

	private void move(int from, int to) {
		Block gateBlock = gateState.getBlock();
		Map<BlockPos, BlockState> oldGate = gateAt(from);
		Map<BlockPos, BlockState> newGate = gateAt(to);
		Set<BlockPos> oldChains = chainsAt(from, oldGate.keySet());
		Set<BlockPos> newChains = chainsAt(to, newGate.keySet());
		BlockState chain = Blocks.CHAIN.getDefaultState();

		// 1. Ta bort grindblock som inte ska vara kvar.
		for (BlockPos q : oldGate.keySet()) {
			if (!newGate.containsKey(q) && world.getBlockState(q).isOf(gateBlock)) {
				world.setBlockState(q, newChains.contains(q) ? chain : Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
		}
		// 2. Linda upp kedjor som inte behövs.
		for (BlockPos q : oldChains) {
			if (!newChains.contains(q) && !newGate.containsKey(q) && world.getBlockState(q).isOf(Blocks.CHAIN)) {
				world.setBlockState(q, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
			}
		}
		// 3. Sätt ut grinden på sin nya plats (bara där det är tomt – annars gömmer den sig i väggen).
		for (Map.Entry<BlockPos, BlockState> entry : newGate.entrySet()) {
			BlockPos q = entry.getKey();
			BlockState current = world.getBlockState(q);
			if (current.isOf(gateBlock)) {
				continue;
			}
			if (canPlaceGateAt(q, current)) {
				if (!current.isAir() && !current.isOf(Blocks.CHAIN)) {
					world.breakBlock(q, true);
				}
				world.setBlockState(q, entry.getValue(), Block.NOTIFY_ALL);
			}
		}
		// 4. Rulla ut kedjor igen.
		for (BlockPos q : newChains) {
			if (world.getBlockState(q).isAir()) {
				world.setBlockState(q, chain, Block.NOTIFY_ALL);
			}
		}
	}

	private boolean canPlaceGateAt(BlockPos q, BlockState current) {
		if (current.isAir()) {
			return true;
		}
		if (current.isOf(Blocks.CHAIN) && chains.contains(q)) {
			return true;
		}
		// Gräs, blommor, snö o.s.v. – men inte vatten eller lava.
		return current.isReplaceable() && current.getFluidState().isEmpty();
	}

	/** Var grindblocken ska vara vid ett visst steg. */
	private Map<BlockPos, BlockState> gateAt(int s) {
		Map<BlockPos, BlockState> result = new HashMap<>();
		for (BlockPos p : gate) {
			BlockPos q = type == GateType.PORTCULLIS ? p.up(s) : rotate(p, s);
			// Grinden får aldrig hamna i höjd med (eller ovanför) vinschen.
			if (q.getY() < pos.getY()) {
				result.put(q, gateState);
			}
		}
		return result;
	}

	/** Var kedjorna ska hänga vid ett visst steg. */
	private Set<BlockPos> chainsAt(int s, Set<BlockPos> occupiedByGate) {
		if (type == GateType.DRAWBRIDGE && s > 0) {
			return Collections.emptySet();
		}
		Set<BlockPos> result = new HashSet<>(chains);
		result.removeAll(occupiedByGate);
		return result;
	}

	/** Roterar ett vindbryggsblock kring gångjärnet (brons nedersta rad), utåt och nedåt. */
	private BlockPos rotate(BlockPos p, int s) {
		if (s == 0) {
			return p;
		}
		double angle = Math.toRadians(90.0 * s / maxSteps);
		int h = p.getY() - hingeY;
		int o = along(p, outward) - hingeOffset;
		double newO = o * Math.cos(angle) + h * Math.sin(angle);
		double newH = h * Math.cos(angle) - o * Math.sin(angle);
		int dO = (int) Math.round(newO) - o;
		int dH = (int) Math.round(newH) - h;
		return p.add(outward.getOffsetX() * dO, dH, outward.getOffsetZ() * dO);
	}

	private static int along(BlockPos p, Direction d) {
		return p.getX() * d.getOffsetX() + p.getZ() * d.getOffsetZ();
	}

	private void playStepSounds(boolean finished) {
		world.playSound(null, pos, SoundEvents.BLOCK_CHAIN_PLACE, SoundCategory.BLOCKS, 1.0f, 0.5f + world.random.nextFloat() * 0.2f);
		BlockPos gatePos = gate.isEmpty() ? pos : gate.get(0);
		if (type == GateType.PORTCULLIS) {
			world.playSound(null, gatePos, SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.6f, 0.5f);
			if (finished && !open) {
				world.playSound(null, gatePos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, SoundCategory.BLOCKS, 0.8f, 0.6f);
			}
		} else {
			world.playSound(null, gatePos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 0.8f, 0.5f);
			if (finished) {
				world.playSound(null, gatePos, SoundEvents.ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR, SoundCategory.BLOCKS, 0.8f, 0.5f);
			}
		}
	}

	// ------------------------------------------------------------------
	// Spara / ladda
	// ------------------------------------------------------------------

	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.writeNbt(nbt, registryLookup);
		nbt.putBoolean("Open", open);
		nbt.putInt("Step", step);
		if (type != null && gateState != null) {
			nbt.putString("Type", type.name());
			nbt.putLongArray("Gate", gate.stream().mapToLong(BlockPos::asLong).toArray());
			nbt.putLongArray("Chains", chains.stream().mapToLong(BlockPos::asLong).toArray());
			nbt.put("GateState", NbtHelper.fromBlockState(gateState));
			nbt.putInt("MaxSteps", maxSteps);
			nbt.putInt("HingeY", hingeY);
			nbt.putInt("HingeOffset", hingeOffset);
			nbt.putInt("Outward", outward.getId());
		}
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
		open = nbt.getBoolean("Open");
		step = nbt.getInt("Step");
		if (nbt.contains("Type")) {
			try {
				type = GateType.valueOf(nbt.getString("Type"));
			} catch (IllegalArgumentException e) {
				type = null;
			}
		} else {
			type = null;
		}
		if (type != null) {
			gate = new ArrayList<>(Arrays.stream(nbt.getLongArray("Gate")).mapToObj(BlockPos::fromLong).toList());
			chains = new ArrayList<>(Arrays.stream(nbt.getLongArray("Chains")).mapToObj(BlockPos::fromLong).toList());
			gateState = NbtHelper.toBlockState(registryLookup.getWrapperOrThrow(RegistryKeys.BLOCK), nbt.getCompound("GateState"));
			maxSteps = nbt.getInt("MaxSteps");
			hingeY = nbt.getInt("HingeY");
			hingeOffset = nbt.getInt("HingeOffset");
			outward = Direction.byId(nbt.getInt("Outward"));
			if (gateState.isAir()) {
				type = null;
			}
		} else {
			gate = new ArrayList<>();
			chains = new ArrayList<>();
			gateState = null;
			step = 0;
		}
	}
}
