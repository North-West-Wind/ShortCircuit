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

    public static Direction relativeDirectionToFacing(RelativeDirection direction, Direction facing) {
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case FRONT -> facing;
            case BACK -> facing.getOpposite();
            case LEFT -> facing.getCounterClockWise();
            case RIGHT -> facing.getClockWise();
        };
    }

    public static Direction circuitBoardFixedDirection(RelativeDirection direction) {
        return switch (direction) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.SOUTH;
            case RIGHT -> Direction.NORTH;
            case FRONT -> Direction.WEST;
            case BACK -> Direction.EAST;
        };
    }
}
