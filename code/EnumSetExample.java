import java.util.EnumSet;
import java.util.Set;

public class EnumSetExample {
  public static void main(String[] args) {
    Set<Direction> set = EnumSet.noneOf(Direction.class);
    set.add(Direction.NORTH);
    set.add(Direction.SOUTH);
    set.add(Direction.NORTH);

    System.out.println(set); // NORTH + SOUTH
    System.out.println(set.contains(Direction.NORTH)); // true

    set.remove(Direction.NORTH);

    System.out.println(set); // SOUTH
    System.out.println(set.contains(Direction.NORTH)); // false
  }
}
