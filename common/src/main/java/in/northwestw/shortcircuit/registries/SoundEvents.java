package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import net.minecraft.sounds.SoundEvent;

import java.util.function.Supplier;

public class SoundEvents {
    public static final Supplier<SoundEvent> TRUTH_ASSIGNED = Services.REGISTRY.registerSound("truth_assigned");

    public static void trigger() { }
}
