package moe.ziyang.jupiter.backend.common;

import java.util.HashMap;
import java.util.Map;

// LRUCache
// LRU 策略缓存，线程不安全
public abstract class LRUCache<T extends Cacheable> implements CachePool<T> {

    // LRU 双向链表中的基础节点
    class Node {
        long key; T value;
        Node prev, next;

        private Node() {
            this.prev = this;
            this.next = this;
        }

        private Node(long key, T value) {
            this();
            this.key = key;
            this.value = value;
        }
    }

    // 最大缓存元素个数。-1 为无限制
    protected final int capacity;
    // dummy head
    private final Node dummy;
    // 数据标识和实际缓存的数据对应
    protected final Map<Long, Node> cache;
    // 缓存中元素的个数
    protected int count = 0;

    protected LRUCache(int capacity) {
        this.capacity = capacity;
        dummy = new Node();
        cache = new HashMap<>();
    }

    // 添加一个元素到缓存中
    // **不会增加 count**
    protected void add(T value) {
        long key = value.getKey();
        Node node = new Node(key, value);
        addNode(node);
        cache.put(key, node);
    }

    // 从缓存中直接获取，当缓存中不存在时返回 null
    @Override
    public T get(long key) {
        if (cache.containsKey(key)) {
            // 资源已经在缓存中了，直接获取并返回
            Node res = cache.get(key);
            removeNode(res);
            addNode(res);
            return res.value;
        }
        return null;
    }

    @Override
    public void release(T value) {
        release(cache.get(value.getKey()));
    }

    @Override
    public void release(long key) {
        release(cache.get(key));
    }

    private void release(Node node) {
        if (node == null) return;
        cache.remove(node.key);
        removeNode(node);
        releaseForCache(node.value);
        node.value.setExpel();
        count --;
    }

    @Override
    public void close() {
        Node current = dummy.prev;
        while (count != 0) {
            if (current == dummy) {
                current = current.prev;
                continue;
            }

            // 尝试获取写锁进行释放
            if (current.value.canRelease()) {
                release(current);
            }

            current = current.prev;
        }
    }

    // 尝试驱逐一个缓存
    protected boolean tryExpelOne() {
        // 从后向前寻找可驱逐的元素
        for (Node current = dummy.prev; current != dummy; current = current.prev) {
            if (current.value.canRelease()) {
                release(current);
                return true;
            }
        }
        return false;
    }

    private void addNode(Node node) {
        dummy.prev.next = node;
        node.prev = dummy.prev;
        node.next = dummy;
        dummy.prev = node;
    }

    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // 当资源不在缓存时的获取行为
    protected abstract T getForCache(long key) throws Exception;

    // 当资源被驱逐时的写回行为
    protected abstract void releaseForCache(T obj);

}
