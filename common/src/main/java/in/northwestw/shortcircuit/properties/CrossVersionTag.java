package in.northwestw.shortcircuit.properties;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

//? if >=1.21.11 {
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? } else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
*///? }

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CrossVersionTag {
    public static class Reader {
        //? if >=1.21.11 {
        private final ValueInput input;

        public Reader(ValueInput input) {
            this.input = input;
        }
        //? } else {
        /*private final CompoundTag input;

        public Reader(CompoundTag input) {
            this.input = input;
        }
        *///? }

        public Optional<UUID> getUUID(String key) {
            //? if >=1.21.11 {
            return this.input.getIntArray(key).map(arr -> UUIDUtil.uuidFromIntArray(arr));
            //? } else
            //return this.input.hasUUID(key) ? Optional.of(this.input.getUUID(key)) : Optional.empty();
        }

        public short getShort(String key, short defaultValue) {
            //? if >=1.21.11 {
            return (short) this.input.getShortOr(key, defaultValue);
            //? } else
            //return this.input.contains(key, Tag.TAG_SHORT) ? this.input.getShort(key) : defaultValue;
        }

        public int getInt(String key, int defaultValue) {
            //? if >=1.21.11 {
            return this.input.getIntOr(key, defaultValue);
            //? } else
            //return this.input.contains(key, Tag.TAG_INT) ? this.input.getInt(key) : defaultValue;
        }

        public boolean getBoolean(String key) {
            return this.getBoolean(key, false);
        }

        public boolean getBoolean(String key, boolean defaultValue) {
            //? if >=1.21.11 {
            return this.input.getBooleanOr(key, defaultValue);
            //? } else
            //return this.input.contains(key, Tag.TAG_BYTE) ? this.input.getBoolean(key) : defaultValue;
        }

        public byte getByte(String key, byte defaultValue) {
            //? if >=1.21.11 {
            return this.input.getByteOr(key, defaultValue);
             //? } else
            //return this.input.contains(key, Tag.TAG_BYTE) ? this.input.getByte(key) : defaultValue;
        }

        public Optional<String> getString(String key) {
            //? if >=1.21.11 {
            return this.input.getString(key);
            //? } else
            //return this.input.contains(key, Tag.TAG_STRING) ? Optional.of(this.input.getString(key)) : Optional.empty();
        }

        public Optional<List<Byte>> getByteArray(String key) {
            //? if >=1.21.11 {
            return this.input.read(key, Codec.BYTE.listOf());
            //? } else {
            /*if (!this.input.contains(key, Tag.TAG_BYTE_ARRAY)) return Optional.empty();
            byte[] arr = this.input.getByteArray(key);
            List<Byte> list = Lists.newArrayList();
            for (byte b : arr) list.add(b);
            return Optional.of(list);
            *///? }
        }

        public List<CrossVersionTag.Reader> getList(String key) {
            //? if >=1.21.11 {
            return this.input.childrenListOrEmpty(key).stream().map(Reader::new).collect(Collectors.toList());
            //? } else
            //return this.input.getList(key, Tag.TAG_COMPOUND).stream().map(tag -> new Reader((CompoundTag) tag)).collect(Collectors.toList());
        }

        public Optional<BlockPos> getBlockPos(String key) {
            //? if >=1.21.11 {
            Optional<int[]> opt = this.input.getIntArray(key);
            if (opt.isEmpty()) return Optional.empty();
            int[] arr = opt.get();
            //? } else {
            /*if (!this.input.contains(key, Tag.TAG_INT_ARRAY)) return Optional.empty();
            int[] arr = this.input.getIntArray(key);
            *///? }
            BlockPos pos = new BlockPos(arr[0], arr[1], arr[2]);
            return Optional.of(pos);
        }

        public Optional<BlockState> getBlockState(String key, HolderGetter<Block> blockGetter) {
            try {
                //? if >=1.21.11 {
                return Optional.of(NbtUtils.readBlockState(blockGetter, NbtUtils.snbtToStructure(this.input.getString(key).orElseThrow())));
                //? } else {
                /*if (!this.input.contains(key, Tag.TAG_STRING)) return Optional.empty();
                return Optional.of(NbtUtils.readBlockState(blockGetter, NbtUtils.snbtToStructure(this.input.getString(key))));
                *///? }
            } catch (Exception e) {
                return Optional.empty();
            }
        }
    }

    public static class Writer {
        //? if >=1.21.11 {
        private final ValueOutput output;

        public Writer(ValueOutput output) {
            this.output = output;
        }
        //? } else {
        /*private final CompoundTag output;

        public Writer(CompoundTag output) {
            this.output = output;
        }
        *///? }

        public void putUUID(String key, UUID uuid) {
            //? if >=1.21.11 {
            this.output.putIntArray(key, UUIDUtil.uuidToIntArray(uuid));
            //? } else
            //this.output.putUUID(key, uuid);
        }

        public void putShort(String key, short value) {
            this.output.putShort(key, value);
        }

        public void putInt(String key, int value) {
            this.output.putInt(key, value);
        }

        public void putBoolean(String key, boolean value) {
            this.output.putBoolean(key, value);
        }

        public void putByte(String key, byte value) {
            this.output.putByte(key, value);
        }

        public void putString(String key, String value) {
            this.output.putString(key, value);
        }

        public void putByteArray(String key, byte[] array) {
            int[] arr = new int[array.length];
            for (int ii = 0; ii < array.length; ii++)
                arr[ii] = array[ii];
            this.output.putIntArray(key, arr);
        }

        public Writer addChild(String key) {
            //? if >= 1.21.11 {
            return new Writer(this.output.childrenList(key).addChild());
            //? } else {
            /*ListTag listTag = this.output.contains(key, Tag.TAG_LIST) ? this.output.getList(key, Tag.TAG_COMPOUND) : new ListTag();
            CompoundTag tag = new CompoundTag();
            listTag.add(tag);
            if (!this.output.contains(key, Tag.TAG_LIST)) this.output.put(key, listTag);
            return new Writer(tag);
            *///? }
        }
    }
}
