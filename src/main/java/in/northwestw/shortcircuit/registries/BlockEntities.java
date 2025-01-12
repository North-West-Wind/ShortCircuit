package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBoardBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.TruthAssignerBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class BlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShortCircuit.MOD_ID);
    public static final Supplier<BlockEntityType<CircuitBlockEntity>> CIRCUIT = BLOCK_ENTITY_TYPES.register("circuit", () -> new BlockEntityType<>(
            CircuitBlockEntity::new,
            Blocks.CIRCUIT.get()
    ));
    public static final Supplier<BlockEntityType<CircuitBoardBlockEntity>> CIRCUIT_BOARD = BLOCK_ENTITY_TYPES.register("circuit_board", () -> new BlockEntityType<>(
            CircuitBoardBlockEntity::new,
            Blocks.CIRCUIT_BOARD.get()
    ));
    public static final Supplier<BlockEntityType<TruthAssignerBlockEntity>> TRUTH_ASSIGNER = BLOCK_ENTITY_TYPES.register("truth_assigner", () -> new BlockEntityType<>(
            TruthAssignerBlockEntity::new,
            Blocks.TRUTH_ASSIGNER.get()
    ));
    public static final Supplier<BlockEntityType<IntegratedCircuitBlockEntity>> INTEGRATED_CIRCUIT = BLOCK_ENTITY_TYPES.register("integrated_circuit", () -> new BlockEntityType<>(
            IntegratedCircuitBlockEntity::new,
            Blocks.INTEGRATED_CIRCUIT.get()
    ));

    public static void registerBlockEntities(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
