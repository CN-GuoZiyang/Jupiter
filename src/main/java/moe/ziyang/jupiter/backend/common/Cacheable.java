package moe.ziyang.jupiter.backend.common;

// Cacheable
// 可被 AbstractCache 缓存的实体需要实现该接口
// 空标识，无实际行为
public interface Cacheable {
    long getKey();
    // 获取元素读锁，返回获取锁是否成功
    boolean readLock();
    void readUnlock();
    // 获取元素写锁，返回获取锁是否成功
    boolean writeLock();
    void writeUnlock();
    // 元素是否可释放，返回 true 时会对元素加写锁
    boolean canRelease();
}
