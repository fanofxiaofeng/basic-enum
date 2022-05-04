# `java.util.EnumMap` 中 增删查改 操作的主线逻辑

我在 [EnumMapExample.java](code/EnumMapExample.java) 中写了一个简单的例子。

代码如下
```java
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
```

在 [code](code) 目录下执行如下命令可以编译+运行
```bash
javac Direction.java EnumMapExample.java
java EnumMapExample
```

结果如下
```text
西门豹
{EAST=东方不败, WEST=西门豹}
```


`java.util.EnumMap` (下文简称为 `EnumMap`)有若干个构造函数，例子里调用的是下面这个
```java
/**
 * Creates an empty enum map with the specified key type.
 *
 * @param keyType the class object of the key type for this enum map
 * @throws NullPointerException if {@code keyType} is null
 */
public EnumMap(Class<K> keyType) {
    this.keyType = keyType;
    keyUniverse = getKeyUniverse(keyType);
    vals = new Object[keyUniverse.length];
}
```

这里出现了 `keyType`, `keyUniverse`, `vals` 3个字段，
我们去 `EnumMap` 里找找它们的 javadoc
```Java
/**
 * The {@code Class} object for the enum type of all the keys of this map.
 *
 * @serial
 */
private final Class<K> keyType;

/**
 * All of the values comprising K.  (Cached for performance.)
 */
private transient K[] keyUniverse;

/**
 * Array representation of this map.  The ith element is the value
 * to which universe[i] is currently mapped, or null if it isn't
 * mapped to anything, or NULL if it's mapped to null.
 */
private transient Object[] vals;
```

可以这么概括
1. `keyType` 里保存 `K` 的类型信息
2. `keyUniverse` 里保存 `K` 类型对应的所有枚举值
3. `vals` 里保存对应的值


再看看 `getKeyUniverse(...)` 是如何实现的
```Java
/**
 * Returns all of the values comprising K.
 * The result is uncloned, cached, and shared by all callers.
 */
private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
    return SharedSecrets.getJavaLangAccess()
                                    .getEnumConstantsShared(keyType);
}
```
从 javadoc 的描述来看，返回值是 `K` 这个类型对应的所有枚举值组成的数组(就 `K` 是 `Direction` 这种情况而言，返回值是 `EAST`, `WEST`, `SOUTH`, `NORTH` 这 `4` 个元素组成的数组)

然后看看基本的增删查改是如何做到的

## 增/改
本文的重心在于理解 `EnumMap` 的整体逻辑(而不是关注其所有细节)，
所以我们在 **增/改** 这个小节里，只看一下普通的 `put(K, V)` 方法是如何实现的。

代码如下
```Java
/**
 * Associates the specified value with the specified key in this map.
 * If the map previously contained a mapping for this key, the old
 * value is replaced.
 *
 * @param key the key with which the specified value is to be associated
 * @param value the value to be associated with the specified key
 *
 * @return the previous value associated with specified key, or
 *     {@code null} if there was no mapping for key.  (A {@code null}
 *     return can also indicate that the map previously associated
 *     {@code null} with the specified key.)
 * @throws NullPointerException if the specified key is null
 */
public V put(K key, V value) {
    typeCheck(key);

    int index = key.ordinal();
    Object oldValue = vals[index];
    vals[index] = maskNull(value);
    if (oldValue == null)
        size++;
    return unmaskNull(oldValue);
}
```

**增/改** 的核心逻辑如下(这里把类型检查，处理 `null` 之类的逻辑当做支线内容，下文中的 **查** 和 **改** 部分也是类似的)
1. 获取 `key` 对应的 `ordinal`
2. 将 `value` 填写到 `vals` 中的对应下标处

## 删
```java
/**
 * Removes the mapping for this key from this map if present.
 *
 * @param key the key whose mapping is to be removed from the map
 * @return the previous value associated with specified key, or
 *     {@code null} if there was no entry for key.  (A {@code null}
 *     return can also indicate that the map previously associated
 *     {@code null} with the specified key.)
 */
public V remove(Object key) {
    if (!isValidKey(key))
        return null;
    int index = ((Enum<?>)key).ordinal();
    Object oldValue = vals[index];
    vals[index] = null;
    if (oldValue != null)
        size--;
    return unmaskNull(oldValue);
}
```

**删** 的核心逻辑如下
1. 获取 `key` 对应的 `ordinal`
2. 将 `vals` 中对应下标处的值赋为 `null`


## 查
```Java
/**
 * Returns the value to which the specified key is mapped,
 * or {@code null} if this map contains no mapping for the key.
 *
 * <p>More formally, if this map contains a mapping from a key
 * {@code k} to a value {@code v} such that {@code (key == k)},
 * then this method returns {@code v}; otherwise it returns
 * {@code null}.  (There can be at most one such mapping.)
 *
 * <p>A return value of {@code null} does not <i>necessarily</i>
 * indicate that the map contains no mapping for the key; it's also
 * possible that the map explicitly maps the key to {@code null}.
 * The {@link #containsKey containsKey} operation may be used to
 * distinguish these two cases.
 */
public V get(Object key) {
    return (isValidKey(key) ?
            unmaskNull(vals[((Enum<?>)key).ordinal()]) : null);
}
```

**查** 的核心逻辑是
1. 获取 `key` 对应的 `ordinal`
2. 获取 `vals` 中对应下标处的值
