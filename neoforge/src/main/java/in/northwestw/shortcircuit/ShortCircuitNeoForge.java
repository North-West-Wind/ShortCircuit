package in.northwestw.shortcircuit;


import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import in.northwestw.shortcircuit.platform.NeoForgeRegistryHelper;
import in.northwestw.shortcircuit.registries.BlockEntityTypes;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import in.northwestw.shortcircuit.registries.blocks.common.CommonCircuitBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.item.BlockItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

//? if >=1.21.11 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
//? } else {
/*import net.minecraft.client.renderer.RenderType;
*///? }

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

    @EventBusSubscriber
    public static class EventHandler {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntityTypes.CIRCUIT.get(), CircuitBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerMenuScreens(RegisterMenuScreensEvent event) {
            event.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new);
        }

        @SubscribeEvent
        public static void itemTooltip(ItemTooltipEvent event) {
            if (event.getItemStack().getItem() instanceof BlockItem item && item.getBlock() instanceof CommonCircuitBlock block)
                event.getToolTip().addAll(block.extraTooltip(event.getItemStack()));
        }

        @SubscribeEvent
        public static void makeTranslucent(FMLClientSetupEvent event) {
            //? if >=1.21.11 {
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.CIRCUIT.get(), ChunkSectionLayer.TRANSLUCENT));
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.INTEGRATED_CIRCUIT.get(), ChunkSectionLayer.TRANSLUCENT));
            //? } else {
            /*event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.CIRCUIT.get(), RenderType.translucent()));
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.INTEGRATED_CIRCUIT.get(), RenderType.translucent()));
            *///? }
        }
    }
}