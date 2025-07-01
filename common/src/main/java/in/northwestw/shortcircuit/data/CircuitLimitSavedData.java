package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.config.Config;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Map;
import java.util.UUID;

public class CircuitLimitSavedData extends SavedData {
    public final Map<UUID, Long> placements;

    public CircuitLimitSavedData() {
        this.placements = Maps.newHashMap();
    }

    public static CircuitLimitSavedData load(CompoundTag tag) {
        CircuitLimitSavedData data = new CircuitLimitSavedData();
        for (Tag tt : tag.getList("placements", ListTag.TAG_COMPOUND)) {
            CompoundTag pair = (CompoundTag) tt;
            data.placements.put(pair.getUUID("uuid"), pair.getLong("amount"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        this.placements.forEach((uuid, amount) -> {
            CompoundTag pair = new CompoundTag();
            pair.putUUID("uuid", uuid);
            pair.putLong("amount", amount);
            list.add(pair);
        });
        tag.put("placements", list);
        return tag;
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
        return storage.computeIfAbsent(CircuitLimitSavedData::load, CircuitLimitSavedData::new, "circuit_limit");
    }
}
