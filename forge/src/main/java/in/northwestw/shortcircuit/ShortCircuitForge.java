package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import in.northwestw.shortcircuit.platform.ForgeRegistryHelper;
import in.northwestw.shortcircuit.registries.BlockEntityTypes;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import in.northwestw.shortcircuit.registries.blocks.common.CommonCircuitBlock;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

//? if >=1.21.11 {
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
//? } else {
/*import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
*///? }

@Mod(ShortCircuitCommon.MOD_ID)
public class ShortCircuitForge {

    public ShortCircuitForge(FMLJavaModLoadingContext context) {
        ShortCircuitCommon.init();

        // register
        //? if >=1.21.11 {
        BusGroup bus = context.getModBusGroup();
        //? } else
        //IEventBus bus = context.getModEventBus();
        ForgeRegistryHelper.BLOCK_ENTITIES.register(bus);
        ForgeRegistryHelper.BLOCKS.register(bus);
        //? if >=1.21.1 {
        ForgeRegistryHelper.CODECS.register(bus);
        ForgeRegistryHelper.DATA_COMPONENTS.register(bus);
        //? }
        ForgeRegistryHelper.ITEMS.register(bus);
        ForgeRegistryHelper.MENUS.register(bus);
        ForgeRegistryHelper.SOUND_EVENTS.register(bus);
        ForgeRegistryHelper.CREATIVE_MODE_TABS.register(bus);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Registries {
        @SubscribeEvent
        public static void clientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> MenuScreens.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new));
            // this should've been set in the block model json, but for whatever reason it refused to work
            //? if >=1.21.11 {
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.CIRCUIT.get(), ChunkSectionLayer.TRANSLUCENT));
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.INTEGRATED_CIRCUIT.get(), ChunkSectionLayer.TRANSLUCENT));
            //? } else {
            /*event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.CIRCUIT.get(), RenderType.translucent()));
            event.enqueueWork(() -> ItemBlockRenderTypes.setRenderLayer(Blocks.INTEGRATED_CIRCUIT.get(), RenderType.translucent()));
            *///? }
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Game {
        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(BlockEntityTypes.CIRCUIT.get(), CircuitBlockEntityRenderer::new);
        }
        
        @SubscribeEvent
        public static void itemTooltip(ItemTooltipEvent event) {
            if (event.getItemStack().getItem() instanceof BlockItem item && item.getBlock() instanceof CommonCircuitBlock block)
                event.getToolTip().addAll(block.extraTooltip(event.getItemStack()));
        }
    }
}