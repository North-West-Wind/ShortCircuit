package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.blocks.IntegratedCircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.TruthAssignerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.PushReaction;

//? if >=1.20.1 {
import net.minecraft.world.level.material.MapColor;
//? } else {
/*import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
*///? }

import java.util.function.Supplier;

public class Blocks {
    public static final Supplier<Block> CIRCUIT = Services.REGISTRY.registerBlock(
            "circuit", CircuitBlock::new,
            //? if >=1.20.1 {
            BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
            //? } else
            //BlockBehaviour.Properties.of(Material.STONE)
                    .instabreak()
                    .sound(SoundType.STONE)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
    );
    public static final Supplier<Block> CIRCUIT_BOARD = Services.REGISTRY.registerBlock(
            "circuit_board", CircuitBoardBlock::new,
            //? if >=1.20.1 {
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND)
            //? } else
            //BlockBehaviour.Properties.of(Material.METAL, MaterialColor.DIAMOND)
                    .lightLevel(state -> 15)
                    .noLootTable()
                    .strength(-1, 3600000)
    );
    public static final Supplier<Block> TRUTH_ASSIGNER = Services.REGISTRY.registerBlock(
            "truth_assigner", TruthAssignerBlock::new,
            //? if >=1.20.1 {
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            //? } else
            //BlockBehaviour.Properties.of(Material.METAL)
                    .requiresCorrectToolForDrops()
                    .strength(3.5F)
                    .sound(SoundType.METAL)
    );
    public static final Supplier<Block> INTEGRATED_CIRCUIT = Services.REGISTRY.registerBlock(
            "integrated_circuit", IntegratedCircuitBlock::new,
            //? if >=1.20.1 {
            BlockBehaviour.Properties.of()
                    .pushReaction(PushReaction.DESTROY)
            //? } else
            //BlockBehaviour.Properties.of(Material.STONE)
                    .instabreak()
                    .sound(SoundType.STONE)
                    .noLootTable()
                    .noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false)
                    .isRedstoneConductor((state, level, pos) -> false)
    );

    public static void trigger() { }
}
