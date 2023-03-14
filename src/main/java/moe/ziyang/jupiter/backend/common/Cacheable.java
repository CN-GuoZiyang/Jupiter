package moe.ziyang.jupiter.backend.common;

// Cacheable
// 可被 AbstractCache 缓存的实体需要实现该接口
// 空标识，无实际行为
public interface Cacheable {
    long getKey();
    // 获取元素读锁
    void readLock();
    // 获取元素写锁
    void writeLock();
    // 元素是否可释放，返回 true 时会对元素加写锁
    boolean canRelease();
    // 设置该元素已被驱逐
    void setExpel();
}
