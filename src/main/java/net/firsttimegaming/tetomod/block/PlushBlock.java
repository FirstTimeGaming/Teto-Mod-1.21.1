package net.firsttimegaming.tetomod.block;

import com.mojang.serialization.MapCodec;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * A decorative plush block that provides a tiered trading interface.
 * <p>
 * When interacted with, the block opens a menu where players can submit
 * items to receive random rewards based on the selected tier.
 */
public class PlushBlock extends BaseEntityBlock {

    // ==================== Class Variables ====================

    /** Minimum X coordinate for the block shape in pixels (0-16 scale). */
    private static final double SHAPE_MIN_X = 4.0D;

    /** Minimum Y coordinate for the block shape in pixels. */
    private static final double SHAPE_MIN_Y = 0.0D;

    /** Minimum Z coordinate for the block shape in pixels. */
    private static final double SHAPE_MIN_Z = 4.0D;

    /** Maximum X coordinate for the block shape in pixels. */
    private static final double SHAPE_MAX_X = 12.5D;

    /** Maximum Y coordinate for the block shape in pixels. */
    private static final double SHAPE_MAX_Y = 12.5D;

    /** Maximum Z coordinate for the block shape in pixels. */
    private static final double SHAPE_MAX_Z = 12.5D;

    /** The voxel shape defining the block's hitbox and collision bounds. */
    private static final VoxelShape SHAPE = Block.box(
            SHAPE_MIN_X, SHAPE_MIN_Y, SHAPE_MIN_Z,
            SHAPE_MAX_X, SHAPE_MAX_Y, SHAPE_MAX_Z
    );

    /** The display name shown in the block's menu. */
    private static final String MENU_TITLE = "Teto";

    /** Codec for serializing this block type. */
    public static final MapCodec<PlushBlock> CODEC = simpleCodec(PlushBlock::new);

    /** The facing direction property for the block. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;


    // ==================== Constructor ====================

    /**
     * Constructs a new PlushBlock with the given properties.
     *
     * @param properties the block properties
     */
    public PlushBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // ==================== Overridden Methods ====================

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PlushBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof PlushBlockEntity plushBlockEntity) {
                plushBlockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof PlushBlockEntity plushBlockEntity) {
            if (!level.isClientSide()) {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(plushBlockEntity, Component.literal(MENU_TITLE)), pos);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
