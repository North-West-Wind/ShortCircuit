package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Blocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ShortCircuit.MOD_ID);
    public static final DeferredBlock<Block> CIRCUIT = BLOCKS.registerBlock(
            "circuit", CircuitBlock::new,
            BlockBehaviour.Properties.of()
                    .instabreak()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn(net.minecraft.world.level.block.Blocks::never)
                    .isRedstoneConductor((state, level, pos) -> false)
    );
    public static final DeferredBlock<Block> CIRCUIT_BOARD = BLOCKS.registerBlock("circuit_board", CircuitBoardBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).lightLevel(state -> 7));

    public static void registerBlocks(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
