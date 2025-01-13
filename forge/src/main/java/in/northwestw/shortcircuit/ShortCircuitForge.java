package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import in.northwestw.shortcircuit.platform.ForgeRegistryHelper;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ShortCircuitCommon.MOD_ID)
public class ShortCircuitForge {

    public ShortCircuitForge(FMLJavaModLoadingContext context) {
        ShortCircuitCommon.init();

        // register
        IEventBus bus = context.getModEventBus();
        ForgeRegistryHelper.BLOCK_ENTITIES.register(bus);
        ForgeRegistryHelper.BLOCKS.register(bus);
        ForgeRegistryHelper.CODECS.register(bus);
        ForgeRegistryHelper.DATA_COMPONENTS.register(bus);
        ForgeRegistryHelper.ITEMS.register(bus);
        ForgeRegistryHelper.MENUS.register(bus);
        ForgeRegistryHelper.SOUND_EVENTS.register(bus);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registries {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntities.CIRCUIT.get(), CircuitBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new));
        }
    }
}