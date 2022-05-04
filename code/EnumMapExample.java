import java.util.EnumMap;
import java.util.Map;

public class EnumMapExample {
  public static void main(String[] args) {
    Map<Direction, String> map = new EnumMap<>(Direction.class);
    map.put(Direction.EAST, "东方朔");
    map.put(Direction.WEST, "西门豹");
    map.remove(Direction.EAST);
    System.out.println(map.get(Direction.WEST));
    map.put(Direction.EAST, "东方不败");
    System.out.println(map);
  }
}
