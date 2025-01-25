package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.blocks.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import java.util.function.Supplier;

public class Blocks {
    public static final Supplier<Block> CIRCUIT = Services.REGISTRY.registerBlock(
            "circuit", CircuitBlock::new,
            BlockBehaviour.Properties.of()
                    .instabreak()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
    );
    public static final Supplier<Block> CIRCUIT_BOARD = Services.REGISTRY.registerBlock(
            "circuit_board", CircuitBoardBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
                    .lightLevel(state -> 15)
                    .noLootTable()
                    .strength(-1, 3600000)
    );
    public static final Supplier<Block> TRUTH_ASSIGNER = Services.REGISTRY.registerBlock(
            "truth_assigner", TruthAssignerBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
    );
    public static final Supplier<Block> INTEGRATED_CIRCUIT = Services.REGISTRY.registerBlock(
            "integrated_circuit", IntegratedCircuitBlock::new,
            BlockBehaviour.Properties.of()
                    .instabreak()
                    .sound(SoundType.STONE)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
    );

    public static final Supplier<Block> INNER_IC = Services.REGISTRY.registerBlock(
            "inner_ic", InnerICBlock::new,
            BlockBehaviour.Properties.of().noLootTable()
    );

    public static void trigger() { }
}
