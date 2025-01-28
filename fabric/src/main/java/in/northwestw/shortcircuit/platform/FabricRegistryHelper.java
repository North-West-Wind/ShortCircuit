package in.northwestw.shortcircuit.platform;

import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.platform.services.IRegistryHelper;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {
    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BlockEntitySupplier<T> factory, Supplier<Block> ...blocks) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        BlockEntityType<T> type = BlockEntityType.register(id.toString(), BlockEntityType.Builder.of(factory::create, Arrays.stream(blocks).map(Supplier::get).toArray(Block[]::new)));
        return () -> type;
    }

    @Override
    public Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        Block block = Registry.register(Registry.BLOCK, id, factory.apply(properties));
        return () -> block;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> function, Item.Properties properties) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        T item = Registry.register(Registry.ITEM, id, function.apply(properties));
        return () -> item;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuSupplier<T> constructor) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        MenuType<T> type = Registry.register(Registry.MENU, id, new MenuType<>(constructor::create));
        return () -> type;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String name) {
        ResourceLocation id = ShortCircuitCommon.rl(name);
        SoundEvent sound = Registry.register(Registry.SOUND_EVENT, id, new SoundEvent(id));
        return () -> sound;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeModeTab(String name, Component title, Supplier<ItemStack> icon, Supplier<? extends Item>... items) {
        CreativeModeTab tab = FabricItemGroupBuilder.create(ShortCircuitCommon.rl(name)).icon(icon).appendItems(list -> {
            for (Supplier<? extends Item> supplier : items) {
                list.add(supplier.get().getDefaultInstance());
            }
        }).build();
        return () -> tab;
    }
}
