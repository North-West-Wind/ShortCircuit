package in.northwestw.shortcircuit.platform;

import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.platform.services.IRegistryHelper;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    public static final Map<ResourceKey<CreativeModeTab>, List<Item>> CREATIVE_MODE_TAB_ITEMS = Maps.newHashMap();

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BlockEntitySupplier<T> factory, Supplier<Block> ...blocks) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        BlockEntityType<T> type = BlockEntityType.register(id.toString(), BlockEntityType.Builder.of(factory::create, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)));
        return () -> type;
    }

    @Override
    public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        Block block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
        return () -> block;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> function, Item.Properties properties) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        T item = Registry.register(BuiltInRegistries.ITEM, id, function.apply(properties));
        return () -> item;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuSupplier<T> constructor, FeatureFlagSet requiredFeatures) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        MenuType<T> type = Registry.register(BuiltInRegistries.MENU, id, new MenuType<>(constructor::create, requiredFeatures));
        return () -> type;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String name) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return () -> sound;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String name, Component title, Supplier<ItemStack> icon, Supplier<? extends Item>... items) {
        ResourceKey<CreativeModeTab> groupKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ShortCircuitCommon.rl(name));
        CREATIVE_MODE_TAB_ITEMS.put(groupKey, (List<Item>) Arrays.stream(items).map(Supplier::get).toList());
        CreativeModeTab tab = FabricItemGroup.builder().icon(icon).title(title).build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, groupKey, tab);
        return () -> tab;
    }
}
