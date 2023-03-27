package moe.ziyang.jupiter.backend.common;

public interface CachePool<T extends Cacheable> {
    // 根据唯一 key 从缓存池获取对象，同时获取对象所有权
    T get(long key);
    // 归还对象所有权
    void release(T value, boolean expel);
    // 根据 key 归还对象所有权
    void release(long value, boolean expel);
    // 关闭缓存池
    void close();
}
