package moe.ziyang.jupiter.backend.common;

public interface CachePool<T extends Cacheable> {
    // 根据唯一 key 从缓存池获取对象
    T get(long key);
    // 释放对象
    void release(T value);
    // 根据 key 释放对象
    void release(long value);
    // 关闭缓存池
    void close();
}
