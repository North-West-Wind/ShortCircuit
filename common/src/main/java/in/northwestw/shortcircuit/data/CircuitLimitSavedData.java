package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.config.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Map;
import java.util.UUID;

public class CircuitLimitSavedData extends SavedData {
    private static final Codec<Pair<UUID, Long>> PAIR_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(Pair::getFirst),
            Codec.LONG.fieldOf("amount").forGetter(Pair::getSecond)
    ).apply(instance, Pair::of));
    public static final Codec<CircuitLimitSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.withAlternative(
                    Codec.unboundedMap(UUIDUtil.CODEC, Codec.LONG),
                    PAIR_CODEC.listOf().xmap(list -> {
                        Map<UUID, Long> map = Maps.newHashMap();
                        for (Pair<UUID, Long> pair : list)
                            map.put(pair.getFirst(), pair.getSecond());
                        return map;
                    }, map -> map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList())
            ).fieldOf("placements").forGetter(data -> data.placements)
    ).apply(instance, CircuitLimitSavedData::new));
    public static final SavedDataType<CircuitLimitSavedData> TYPE = new SavedDataType<>("circuit_limit", CircuitLimitSavedData::new, CODEC, null);
    public final Map<UUID, Long> placements;

    public CircuitLimitSavedData() {
        this(Maps.newHashMap());
    }

    public CircuitLimitSavedData(Map<UUID, Long> placements) {
        this.placements = placements;
    }

    public boolean canAdd(UUID uuid) {
        return Config.MAX_CIRCUITS_PER_PLAYER <= 0 || this.placements.getOrDefault(uuid, 0L) < Config.MAX_CIRCUITS_PER_PLAYER;
    }

    public void add(UUID uuid) {
        this.placements.put(uuid, this.placements.getOrDefault(uuid, 0L) + 1);
    }

    public void remove(UUID uuid) {
        long amount = this.placements.getOrDefault(uuid, 1L) - 1;
        if (amount <= 0) this.placements.remove(uuid);
        else this.placements.put(uuid, amount);
    }

    public static CircuitLimitSavedData getRuntimeData(ServerLevel level) {
        return CircuitLimitSavedData.getRuntimeData(level.getServer());
    }

    public static CircuitLimitSavedData getRuntimeData(MinecraftServer server) {
        ServerLevel runtimeLevel = server.getLevel(Constants.RUNTIME_DIMENSION);
        DimensionDataStorage storage = runtimeLevel.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }
}
