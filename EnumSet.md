# `java.util.EnumSet` 中增删查操作的主线逻辑

**尚未完工**

我在 [EnumSetExample.java](code/EnumSetExample.java) 中写了一个简单的例子。

代码如下

```Java
import java.util.EnumSet;
import java.util.Set;

public class EnumMapExample {
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
```

在 [code](code) 目录下执行如下命令可以编译+运行
```bash
javac Direction.java EnumSetExample.java
java EnumSetExample
```

结果如下
```text
[SOUTH, NORTH]
true
[SOUTH]
false
```


和 `java.util.EnumMap` 不同， `java.util.EnumSet` (下文简称为 `EnumSet`)是 `abstract` 的，
所以不能直接生成 EnumSet 类的实例(只能生成其子类的实例)。
上面的例子中是通过调用一个静态方法来获取 `EnumSet` 实例的。
此方法的代码如下

```Java
/**
 * Creates an empty enum set with the specified element type.
 *
 * @param <E> The class of the elements in the set
 * @param elementType the class object of the element type for this enum
 *     set
 * @return An empty enum set of the specified type.
 * @throws NullPointerException if {@code elementType} is null
 */
public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    Enum<?>[] universe = getUniverse(elementType);
    if (universe == null)
        throw new ClassCastException(elementType + " not an enum");

    if (universe.length <= 64)
        return new RegularEnumSet<>(elementType, universe);
    else
        return new JumboEnumSet<>(elementType, universe);
}
```

先看主线逻辑
1. 生成 universe
2. 如果 universe 的元素数 <= 64，则返回 RegularEnumSet 的实例； 否则返回 JumboEnumSet 的实例

然后再展开说
`getUniverse(...)` 方法在 [EnumMap.md](EnumMap.md) 中提到过，
其作用是将对应的枚举值作为一个数组返回(就 `Direction` 而言，返回值是 `EAST`, `WEST`, `SOUTH`, `NORTH` 这 `4` 个元素组成的数组)。



```Java
/**
 * Returns all of the values comprising E.
 * The result is uncloned, cached, and shared by all callers.
 */
private static <E extends Enum<E>> E[] getUniverse(Class<E> elementType) {
    return SharedSecrets.getJavaLangAccess()
                                    .getEnumConstantsShared(elementType);
}
```

当参数 elementType 为 Direction.class 时，universe 里就是 `EAST`, `WEST`, `SOUTH`, `NORTH` 这4个元素。

然后就出现了两个种情况
## RegularEnumSet
字面意思是常规的枚举集，当 universe 中的元素数 <= 64 时，返回的都是 RegularEnumSet 的实例。
先看看它的 javadoc

```Java
/**
 * Private implementation class for EnumSet, for "regular sized" enum types
 * (i.e., those with 64 or fewer enum constants).
 *
 * @author Josh Bloch
 * @since 1.5
 * @serial exclude
 */
class RegularEnumSet<E extends Enum<E>> extends EnumSet<E> {
    ...(其他内容略)

    /**
     * Bit vector representation of this set.  The 2^k bit indicates the
     * presence of universe[k] in this set.
     */
    private long elements = 0L;

    ...(其他内容略)
}
```

它有一个名为 `elements` 的字段，看来是把 elements 看成一堆bit了(每个bit都可以和一个枚举值对应。对 `Direction` 而言，我们只会用到 elements 的最低4位)


让我们看看它的增删查是如何实现的吧
### 增
```Java
/**
 * Adds the specified element to this set if it is not already present.
 *
 * @param e element to be added to this set
 * @return {@code true} if the set changed as a result of the call
 *
 * @throws NullPointerException if {@code e} is null
 */
public boolean add(E e) {
    typeCheck(e);

    long oldElements = elements;
    elements |= (1L << ((Enum<?>)e).ordinal());
    return elements != oldElements;
}
```
看来是位运算。

Set<Direction> set = EnumSet.noneOf(Direction.class);
0x0000000000000000 `0` 太多了，看着头晕，不如拆开看吧(16个bit作为一组，一共分为4组)

0x 0000 0000 0000 0000
0x 0000 0000 0000 0001
0x 0000 0000 0000 0002
0x 0000 0000 0000 0004
0x 0000 0000 0000 0008


set.add(Direction.NORTH);


## JumboEnumSet
