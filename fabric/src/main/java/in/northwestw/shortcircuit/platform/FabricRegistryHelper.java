package in.northwestw.shortcircuit.platform;

import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.platform.services.IRegistryHelper;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

//? if >=1.21.1 {
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.function.UnaryOperator;
//? }

//? if >=1.20.1 {
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
//? } else {
/*import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
*///? }

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    public static final Map<ResourceKey<CreativeModeTab>, List<Item>> CREATIVE_MODE_TAB_ITEMS = Maps.newHashMap();

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BlockEntitySupplier<T> factory, Supplier<Block> ...blocks) {
        Identifier id = ShortCircuitCommon.rl(name);
        Block[] gotBlocks = Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new);
        BlockEntityType<T> type = Registry.register(
                //~ if <=1.19.2 'BuiltInRegistries' -> 'Registry'
                BuiltInRegistries.BLOCK_ENTITY_TYPE, id,
                FabricBlockEntityTypeBuilder.create(factory::create, gotBlocks).build()
        );
        return () -> type;
    }

    @Override
    public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Identifier id = ShortCircuitCommon.rl(name);
        //? if >=1.21.4 {
        properties.setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), id));
        //? }
        //~ if <=1.19.2 'BuiltInRegistries.BLOCK' -> 'Registry.BLOCK'
        Block block = Registry.register(BuiltInRegistries.BLOCK, id, factory.apply(properties));
        return () -> block;
    }

    //? if >=1.21.1 {
    @Override
    public <T extends Block> Supplier<MapCodec<T>> registerCodec(String name, Supplier<MapCodec<T>> supplier) {
        Identifier id = ShortCircuitCommon.rl(name);
        MapCodec<T> codec = Registry.register(BuiltInRegistries.BLOCK_TYPE, id, supplier.get());
        return () -> codec;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        Identifier id = ShortCircuitCommon.rl(name);
        DataComponentType<T> type = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, id, builder.apply(DataComponentType.builder()).build());
        return () -> type;
    }
    //? }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> function, Item.Properties properties) {
        Identifier id = ShortCircuitCommon.rl(name);
        //? if >=1.21.4 {
        properties.setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id));
        //? }
        //~ if <=1.19.2 'BuiltInRegistries.ITEM' -> 'Registry.ITEM'
        T item = Registry.register(BuiltInRegistries.ITEM, id, function.apply(properties));
        return () -> item;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuSupplier<T> constructor) {
        Identifier id = ShortCircuitCommon.rl(name);
        //? if >=1.20.1 {
        MenuType<T> type = Registry.register(BuiltInRegistries.MENU, id, new MenuType<>(constructor::create, FeatureFlags.DEFAULT_FLAGS));
        //? } else
        //MenuType<T> type = Registry.register(Registry.MENU, id, new MenuType<>(constructor::create));
        return () -> type;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String name) {
        Identifier id = ShortCircuitCommon.rl(name);
        //~ if <=1.19.2 'BuiltInRegistries.SOUND_EVENT' -> 'Registry.SOUND_EVENT'
        //~ if <=1.19.2 'SoundEvent.createVariableRangeEvent' -> 'new SoundEvent'
        SoundEvent sound = Registry.register(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
        return () -> sound;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String name, Component title, Supplier<ItemStack> icon, Supplier<? extends Item>... items) {
        //? if >=1.20.1 {
        ResourceKey<CreativeModeTab> groupKey = ResourceKey.create(BuiltInRegistries.CREATIVE_MODE_TAB.key(), ShortCircuitCommon.rl(name));
        CREATIVE_MODE_TAB_ITEMS.put(groupKey, (List<Item>) Arrays.stream(items).map(Supplier::get).toList());
        CreativeModeTab tab = FabricItemGroup.builder().icon(icon).title(title).build();
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, groupKey, tab);
        //? } else {
        /*CreativeModeTab tab = FabricItemGroupBuilder.create(ShortCircuitCommon.rl(name)).icon(icon).appendItems(list -> {
            for (Supplier<? extends Item> supplier : items) {
                list.add(supplier.get().getDefaultInstance());
            }
        }).build();
        *///? }
        return () -> tab;
    }
}
