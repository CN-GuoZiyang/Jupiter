package moe.ziyang.jupiter.backend.common;

// Cacheable
// 可被 AbstractCache 缓存的实体需要实现该接口
// 空标识，无实际行为
public interface Cacheable {
    // 获取元素的 key
    long getKey();
    // 获取元素所有权
    void acquireUsage();
    // 归还元素所有权
    void returnUsage();
    // 元素是否可释放，若可释放则获取并返回 true
    boolean canExpel();

}
