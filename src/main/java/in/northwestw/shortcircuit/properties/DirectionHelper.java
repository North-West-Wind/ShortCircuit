package in.northwestw.shortcircuit.properties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class DirectionHelper {
    public static RelativeDirection directionToRelativeDirection(Direction facing, Direction direction) {
        if (direction == Direction.UP) return RelativeDirection.UP;
        if (direction == Direction.DOWN) return RelativeDirection.DOWN;
        // if they face the same way, it's front
        int offset = direction.get2DDataValue() - facing.get2DDataValue();
        if (offset < 0) offset += 4;
        return switch (offset) {
            case 0 -> RelativeDirection.FRONT;
            case 1 -> RelativeDirection.RIGHT;
            case 2 -> RelativeDirection.BACK;
            default ->  RelativeDirection.LEFT;
        };
    }

    public static Direction getDirectionFromPosToPos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return a.getX() - b.getX() == 1 ? Direction.WEST : Direction.EAST;
        if (a.getY() != b.getY()) return a.getY() - b.getY() == 1 ? Direction.DOWN : Direction.UP;
        return a.getZ() - b.getZ() == 1 ? Direction.NORTH : Direction.SOUTH;
    }

    public static Direction relativeDirectionToFacing(RelativeDirection direction, Direction facing) {
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getClockWise();
            case RIGHT -> facing.getCounterClockWise();
        };
    }
}
