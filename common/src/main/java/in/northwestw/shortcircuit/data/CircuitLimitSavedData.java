package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
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
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CircuitLimitSavedData extends SavedData {
    public static final Codec<CircuitLimitSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Constants.pairMapCodec(UUIDUtil.CODEC, Codec.LONG, "uuid", "amount").fieldOf("placements").forGetter(CircuitLimitSavedData::flattenPlacements)
    ).apply(instance, CircuitLimitSavedData::new));
    public static final SavedDataType<CircuitLimitSavedData> TYPE = new SavedDataType<>("circuit_limit", CircuitLimitSavedData::new, CODEC, null);
    public final Map<UUID, Long> placements;

    public CircuitLimitSavedData() {
        this.placements = Maps.newHashMap();
    }

    public CircuitLimitSavedData(List<Pair<UUID, Long>> placements) {
        this();
        for (Pair<UUID, Long> pair : placements)
            this.placements.put(pair.getLeft(), pair.getRight());
    }

    public List<Pair<UUID, Long>> flattenPlacements() {
        return this.placements.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
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
