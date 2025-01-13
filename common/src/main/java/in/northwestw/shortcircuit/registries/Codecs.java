package in.northwestw.shortcircuit.registries;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.blocks.IntegratedCircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.TruthAssignerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Supplier;

public class Codecs {
    public static final Supplier<MapCodec<CircuitBlock>> CIRCUIT = Services.REGISTRY.registerCodec("circuit", () -> BlockBehaviour.simpleCodec(CircuitBlock::new));
    public static final Supplier<MapCodec<CircuitBoardBlock>> CIRCUIT_BOARD = Services.REGISTRY.registerCodec("circuit_board", () -> BlockBehaviour.simpleCodec(CircuitBoardBlock::new));
    public static final Supplier<MapCodec<IntegratedCircuitBlock>> INTEGRATED_CIRCUIT = Services.REGISTRY.registerCodec("integrated_circuit", () -> BlockBehaviour.simpleCodec(IntegratedCircuitBlock::new));
    public static final Supplier<MapCodec<TruthAssignerBlock>> TRUTH_ASSIGNER = Services.REGISTRY.registerCodec("truth_assigner", () -> BlockBehaviour.simpleCodec(TruthAssignerBlock::new));

    public static void trigger() { }
}
