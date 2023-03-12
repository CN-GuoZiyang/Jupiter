package moe.ziyang.jupiter.backend.common;

import moe.ziyang.jupiter.common.Error;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// 通用缓存框架，实现 LRU 策略
public abstract class AbstractCache<T extends Cacheable> {

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
    private final int capacity;
    // dummy head
    private final Node dummy;
    // 数据标识和实际缓存的数据对应
    private final Map<Long, Node> cache;

    // 正在获取的资源
    private final Set<Long> getting;

    // 缓存中元素的个数
    private int count = 0;
    // 缓存池的锁
    private final Lock lock;

    public AbstractCache(int capacity) {
        this.capacity = capacity;
        dummy = new Node();
        cache = new HashMap<>();
        getting = new HashSet<>();
        lock = new ReentrantLock();
    }

    // 从缓存中获取一个资源，如果不存在就尝试驱逐驱逐一个已有元素
    // 默认对元素加读锁，writeLock 为 true 时加写锁
    protected T get(long key, boolean writeLock) throws Exception {
        while(true) {
            lock.lock();
            if (getting.contains(key)) {
                // 请求的资源正在被其他资源获取
                lock.unlock();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            if (cache.containsKey(key)) {
                // 资源已经在缓存中了，直接获取并返回
                Node res = cache.get(key);
                remove(res);
                add(res);
                lock.unlock();
                return res.value;
            }

            // 都不在，尝试从源获取资源
            if (capacity > 0 && count == capacity) {
                // 已达到缓存最大使用量，尝试驱逐一个元素
                if (!tryExpelOneElement()) {
                    lock.unlock();
                    throw Error.CacheFullException;
                }
            }
            count ++;
            getting.add(key);
            lock.unlock();
            break;
        }

        // 从源获取资源
        T obj;
        try {
            obj = getForCache(key);
        } catch (Exception e) {
            lock.lock();
            count --;
            getting.remove(key);
            lock.unlock();
            throw e;
        }

        // 将资源添加至资源池
        lock.lock();
        getting.remove(key);
        Node res = new Node(key, obj);
        add(res);
        cache.put(key, res);
        lock.unlock();

        return obj;
    }

    // 关闭缓存并写回
    protected void close() {
        lock.lock();
        try {
            Node current = dummy.prev;
            while (count != 0) {
                if (current == dummy) {
                    current = current.prev;
                    continue;
                }

                // 尝试获取写锁进行释放
                if (current.value.tryLock()) {
                    cache.remove(current.key);
                    remove(current);
                    releaseForCache(current.value);
                    count --;
                }

                current = current.prev;
            }
        } finally {
            lock.unlock();
        }

    }

    // 尝试驱逐一个元素，逐出成功则返回 true
    private boolean tryExpelOneElement() {
        // 从后向前寻找可驱逐的元素
        for (Node current = dummy.prev; current != dummy; current = current.prev) {
            if (current.value.tryLock()) {
                cache.remove(current.key);
                remove(current);
                releaseForCache(current.value);
                count --;
                return true;
            }
        }
        return false;
    }

    private void add(Node node) {
        dummy.prev.next = node;
        node.prev = dummy.prev;
        node.next = dummy;
        dummy.prev = node;
    }

    private void remove(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }


    // 当资源不在缓存时的获取行为
    protected abstract T getForCache(long key) throws Exception;

    // 当资源被驱逐时的写回行为
    protected abstract void releaseForCache(T obj);

}
