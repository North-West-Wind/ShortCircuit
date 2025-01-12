package in.northwestw.shortcircuit.registries;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.blocks.IntegratedCircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.TruthAssignerBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Codecs {
    public static final DeferredRegister<MapCodec<? extends Block>> CODECS = DeferredRegister.create(BuiltInRegistries.BLOCK_TYPE, ShortCircuit.MOD_ID);
    public static final Supplier<MapCodec<CircuitBlock>> CIRCUIT = CODECS.register("circuit", () -> BlockBehaviour.simpleCodec(CircuitBlock::new));
    public static final Supplier<MapCodec<CircuitBoardBlock>> CIRCUIT_BOARD = CODECS.register("circuit_board", () -> BlockBehaviour.simpleCodec(CircuitBoardBlock::new));
    public static final Supplier<MapCodec<IntegratedCircuitBlock>> INTEGRATED_CIRCUIT = CODECS.register("integrated_circuit", () -> BlockBehaviour.simpleCodec(IntegratedCircuitBlock::new));
    public static final Supplier<MapCodec<TruthAssignerBlock>> TRUTH_ASSIGNER = CODECS.register("truth_assigner", () -> BlockBehaviour.simpleCodec(TruthAssignerBlock::new));

    public static void registerCodecs(IEventBus bus) {
        CODECS.register(bus);
    }
}
