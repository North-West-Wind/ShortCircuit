package in.northwestw.shortcircuit.platform.services;

import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface IRegistryHelper {
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntityType(String name, BlockEntitySupplier<T> factory, Supplier<Block> ...blocks);
    Supplier<Block> registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties);
    <T extends Block> Supplier<MapCodec<T>> registerCodec(String name, Supplier<MapCodec<T>> supplier);
    <T> Supplier<DataComponentType<T>> registerDataComponent(String name, UnaryOperator<DataComponentType.Builder<T>> builder);
    <T extends Item> Supplier<T> registerItem(String name, Function<Item.Properties, T> function, Item.Properties properties);
    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String name, MenuSupplier<T> constructor, FeatureFlagSet requiredFeatures);
    Supplier<SoundEvent> registerSound(String name);
    Supplier<CreativeModeTab> registerCreativeModeTab(String name, Component title, Supplier<ItemStack> icon, Supplier<? extends Item> ...items);

    @FunctionalInterface
    interface BlockEntitySupplier<T extends BlockEntity> {
        T create(BlockPos var1, BlockState var2);
    }

    interface MenuSupplier<T extends AbstractContainerMenu> {
        T create(int var1, Inventory var2);
    }
}
