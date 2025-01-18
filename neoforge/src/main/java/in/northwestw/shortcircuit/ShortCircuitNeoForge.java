package in.northwestw.shortcircuit;


import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import in.northwestw.shortcircuit.platform.NeoForgeRegistryHelper;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import in.northwestw.shortcircuit.registries.blockentityrenderers.IntegratedCircuitBlockEntityRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(ShortCircuitCommon.MOD_ID)
public class ShortCircuitNeoForge {
    public ShortCircuitNeoForge(IEventBus bus) {
        ShortCircuitCommon.init();
        NeoForgeRegistryHelper.BLOCK_ENTITIES.register(bus);
        NeoForgeRegistryHelper.BLOCKS.register(bus);
        NeoForgeRegistryHelper.CODECS.register(bus);
        NeoForgeRegistryHelper.DATA_COMPONENTS.register(bus);
        NeoForgeRegistryHelper.ITEMS.register(bus);
        NeoForgeRegistryHelper.MENUS.register(bus);
        NeoForgeRegistryHelper.SOUND_EVENTS.register(bus);
        NeoForgeRegistryHelper.CREATIVE_MODE_TABS.register(bus);
    }

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
    public static class Registries {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntities.CIRCUIT.get(), CircuitBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(BlockEntities.INTEGRATED_CIRCUIT.get(), IntegratedCircuitBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new);
        }
    }
}