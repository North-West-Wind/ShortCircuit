package in.northwestw.shortcircuit;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class Constants {
    public static final ResourceKey<Level> CIRCUIT_BOARD_DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "circuit_board"));
    public static final ResourceKey<Level> RUNTIME_DIMENSION = ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "runtime"));
}
