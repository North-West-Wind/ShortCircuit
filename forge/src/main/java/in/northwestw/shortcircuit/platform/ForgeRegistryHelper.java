package in.northwestw.shortcircuit.platform;

import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.platform.services.IRegistryHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
import net.minecraftforge.registries.DeferredRegister;

//? if >=1.21.1 {
import com.mojang.serialization.MapCodec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;
import java.util.function.UnaryOperator;
//? }

//? if >=1.20.1 {
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTabs;
//? } else {
/*import net.minecraft.core.NonNullList;
import net.minecraftforge.registries.ForgeRegistries;
*///? }

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ForgeRegistryHelper implements IRegistryHelper {
    //~ if <=1.19.2 'Registries.BLOCK_ENTITY_TYPE' -> 'ForgeRegistries.BLOCK_ENTITY_TYPES'
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ShortCircuitCommon.MOD_ID);
    //~ if <=1.19.2 'Registries.BLOCK' -> 'ForgeRegistries.BLOCKS'
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ShortCircuitCommon.MOD_ID);
    //? if >=1.21.1 {
    public static final DeferredRegister<MapCodec<? extends Block>> CODECS = DeferredRegister.create(Registries.BLOCK_TYPE, ShortCircuitCommon.MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, ShortCircuitCommon.MOD_ID);
    //? }
    //~ if <=1.19.2 'Registries.ITEM' -> 'ForgeRegistries.ITEMS'
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ShortCircuitCommon.MOD_ID);
    //~ if <=1.19.2 'Registries.MENU' -> 'ForgeRegistries.MENU_TYPES'
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ShortCircuitCommon.MOD_ID);
    //~ if <=1.19.2 'Registries.SOUND_EVENT' -> 'ForgeRegistries.SOUND_EVENTS'
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ShortCircuitCommon.MOD_ID);
    //? if >=1.20.1 {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShortCircuitCommon.MOD_ID);
    //? }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BlockEntitySupplier<T> factory, Supplier<Block> ...blocks) {
        //? if >=1.21.4 {
        return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(factory::create, Arrays.stream(blocks).map(Supplier::get).collect(Collectors.toSet())));
        //? } else
        //return BLOCK_ENTITIES.register(name, () -> new BlockEntityType<>(factory::create, Arrays.stream(blocks).map(Supplier::get).collect(Collectors.toSet()), null));
    }

    @Override
    public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        //? if >=1.21.4 {
        properties.setId(ResourceKey.create(Registries.BLOCK, ShortCircuitCommon.rl(name)));
        //? }
        return BLOCKS.register(name, () -> factory.apply(properties));
    }

    //? if >=1.21.1 {
    @Override
    public <T extends Block> Supplier<MapCodec<T>> registerCodec(String name, Supplier<MapCodec<T>> supplier) {
        return CODECS.register(name, supplier);
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }
    //? }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> function, Item.Properties properties) {
        //? if >=1.21.4 {
        properties.setId(ResourceKey.create(Registries.ITEM, ShortCircuitCommon.rl(name)));
        //? }
        return ITEMS.register(name, () -> function.apply(properties));
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuSupplier<T> constructor) {
        //? if >=1.20.1 {
        return MENUS.register(name, () -> new MenuType<>(constructor::create, FeatureFlags.DEFAULT_FLAGS));
        //? } else
        //return MENUS.register(name, () -> new MenuType<>(constructor::create));
    }

    @Override
    public Supplier<SoundEvent> registerSound(String name) {
        //~ if <=1.19.2 'SoundEvent.createVariableRangeEvent' -> 'new SoundEvent'
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ShortCircuitCommon.rl(name)));
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String name, Component title, Supplier<ItemStack> icon, Supplier<? extends Item>... items) {
        //? if >=1.20.1 {
        return CREATIVE_MODE_TABS.register(name, () -> CreativeModeTab.builder().withTabsBefore(CreativeModeTabs.COMBAT).title(title).icon(icon).displayItems((params, output) -> {
            for (Supplier<? extends Item> item : items)
                output.accept(item.get());
        }).build());
        //? } else {
        /*CreativeModeTab tab = new CreativeModeTab(name) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }

            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public void fillItemList(NonNullList<ItemStack> list) {
                for (Supplier<? extends Item> supplier : items) {
                    list.add(supplier.get().getDefaultInstance());
                }
            }
        };
        return () -> tab;
        *///? }
    }
}
