package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import in.northwestw.shortcircuit.platform.FabricRegistryHelper;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentityrenderers.CircuitBlockEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Map;

public class ShortCircuitFabric implements ModInitializer, ClientModInitializer {
    @Override
    public void onInitialize() {
        ShortCircuitCommon.init();
        for (Map.Entry<ResourceKey<CreativeModeTab>, List<Item>> entry : FabricRegistryHelper.CREATIVE_MODE_TAB_ITEMS.entrySet())
            ItemGroupEvents.modifyEntriesEvent(entry.getKey()).register(group ->
                    group.addAfter((stack) -> true,
                            entry.getValue().stream().map(Item::getDefaultInstance).toList(),
                            CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS)
            );
    }

    @Override
    public void onInitializeClient() {
        BlockEntityRenderers.register(BlockEntities.CIRCUIT.get(), CircuitBlockEntityRenderer::new);
        MenuScreens.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new);
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.CIRCUIT.get(), RenderType.translucent());
        BlockRenderLayerMap.INSTANCE.putBlock(Blocks.INTEGRATED_CIRCUIT.get(), RenderType.translucent());
    }
}
