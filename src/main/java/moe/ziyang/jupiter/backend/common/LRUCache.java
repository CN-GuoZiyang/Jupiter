package moe.ziyang.jupiter.backend.common;

import moe.ziyang.jupiter.common.DBError;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    // 正在获取中的元素
    private final Map<Long, Object> getting;

    // 缓存池锁
    private Lock lock;

    protected LRUCache(int capacity) {
        this.capacity = capacity;
        dummy = new Node();
        cache = new HashMap<>();
        getting = new HashMap<>();
        lock = new ReentrantLock();
    }

    // 从缓存中直接获取，当缓存中不存在时返回 null
    @Override
    public T get(long key) throws DBError {
        while (true) {
            lock.lock();

            // 资源已经在缓存中了，直接获取并返回
            if (cache.containsKey(key)) {
                try {
                    Node res = cache.get(key);
                    removeNode(res);
                    addNode(res);
                    return res.value;
                } finally {
                    lock.unlock();
                }
            }

            // 请求的资源正在被其他线程获取
            Object obj = getting.get(key);
            if (obj != null) {
                lock.unlock();
                try {
                    obj.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }

            // 都不在，解锁，准备从源获取
            // 如果缓存已满，尝试驱逐一个元素
            if (count == capacity) {
                if (!tryExpelOne()) throw DBError.CacheFullException;
            }
            count ++;
            getting.put(key, new Object());
            lock.unlock();
            break;
        }

        // 资源不在缓存中，从源获取
        T resource;
        try {
            resource = getFromSource(key);
        } catch (DBError e) {
            // 获取失败，解除获取状态
            lock.unlock();
            count --;
            getting.remove(key).notifyAll();
            lock.unlock();
            throw e;
        }

        // 获取成功，写入缓存，并直接获取该资源的所有权
        lock.lock();
        try {
            addNode(new Node(key, resource));
            getting.remove(key).notifyAll();
            resource.acquireUsage();
            return resource;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void release(T value) {
        innerRelease(cache.get(value.getKey()));
    }

    @Override
    public void release(long key) {
        innerRelease(cache.get(key));
    }

    @Override
    public void close() {
        lock.lock();
        try {
            Node current = dummy.prev;
            while (count != 0) {
                if (current == dummy) {
                    current = current.prev;
                    continue;
                }

                // 尝试进行驱逐
                if (current.value.canExpel()) {
                    expel(current);
                }

                current = current.prev;
            }
        } finally {
            lock.unlock();
        }
    }

    // 释放元素所有权
    private void innerRelease(Node node) {
        if (node == null) return;
        node.value.returnUsage();
    }

    // 尝试驱逐一个缓存
    protected boolean tryExpelOne() {
        // 从后向前寻找可驱逐的元素
        for (Node current = dummy.prev; current != dummy; current = current.prev) {
            if (current.value.canExpel()) {
                expel(current);
                return true;
            }
        }
        return false;
    }

    // 驱逐一个节点，调用方加锁
    private void expel(Node node) {
        if (node == null) return;
        cache.remove(node.key);
        removeNode(node);
        returnToSource(node.value);
        count --;
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
    protected abstract T getFromSource(long key) throws DBError;

    // 当资源被驱逐时的写回行为
    protected abstract void returnToSource(T obj);

}
