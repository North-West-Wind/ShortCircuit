package in.northwestw.shortcircuit.properties;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum RelativeDirection implements StringRepresentable {
    UP("up", 0),
    DOWN("down", 1),
    LEFT("left", 2),
    RIGHT("right", 3),
    FRONT("front", 4),
    BACK("back", 5);

    public static final EnumProperty<RelativeDirection> REL_DIRECTION = EnumProperty.create("rel_dir", RelativeDirection.class);

    final String name;
    final byte id;

    RelativeDirection(String name, int id) {
        this.name = name;
        this.id = (byte) id;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public byte getId() {
        return id;
    }

    public static RelativeDirection fromName(String name) {
        for (RelativeDirection dir : RelativeDirection.values()) {
            if (dir.getSerializedName().equals(name)) return dir;
        }
        return null;
    }

    public static RelativeDirection fromId(byte id) {
        for (RelativeDirection dir : RelativeDirection.values()) {
            if (dir.getId() == id) return dir;
        }
        return null;
    }
}
