/*
 * Decompiled with CFR 0.152.
 */
public final class Direction
extends Enum<Direction> {
    public static final /* enum */ Direction EAST = new Direction("EAST", 0);
    public static final /* enum */ Direction WEST = new Direction("WEST", 1);
    public static final /* enum */ Direction SOUTH = new Direction("SOUTH", 2);
    public static final /* enum */ Direction NORTH = new Direction("NORTH", 3);
    private static final /* synthetic */ Direction[] $VALUES;

    public static Direction[] values() {
        return (Direction[])$VALUES.clone();
    }

    public static Direction valueOf(String string) {
        return Enum.valueOf(Direction.class, string);
    }

    private Direction(String string, int n) {
        super(string, n);
    }

    private static /* synthetic */ Direction[] $values() {
        return new Direction[]{EAST, WEST, SOUTH, NORTH};
    }

    static {
        $VALUES = Direction.$values();
    }
}
