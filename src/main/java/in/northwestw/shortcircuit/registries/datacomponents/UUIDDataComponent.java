package in.northwestw.shortcircuit.registries.datacomponents;

import java.util.UUID;

public record UUIDDataComponent(UUID uuid) {
    public static UUIDDataComponent fromLongs(long msb, long lsb) {
        return new UUIDDataComponent(new UUID(msb, lsb));
    }

    public long mostSignificantBits() {
        return this.uuid.getMostSignificantBits();
    }

    public long leastSignificantBits() {
        return this.uuid.getLeastSignificantBits();
    }
}