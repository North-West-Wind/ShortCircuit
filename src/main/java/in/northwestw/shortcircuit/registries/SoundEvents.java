package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class SoundEvents {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ShortCircuit.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TRUTH_ASSIGNED = SOUND_EVENTS.register("truth_assigned", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(ShortCircuit.MOD_ID, "truth_assigned")));

    public static void registerSoundEvents(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
