package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.common.LRUCache;
import moe.ziyang.jupiter.backend.dm.page.Page;
import moe.ziyang.jupiter.common.Error;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageCacheImpl extends LRUCache<Page> implements PageCache {

    // 正在获取的资源
    private final Set<Long> getting;
    // 页池锁
    private Lock lock;

    protected PageCacheImpl(int capacity) {
        super(capacity);
        getting = new HashSet<>();
        lock = new ReentrantLock();
    }

    // 获取页
    public Page get(int pgno) throws Exception {
        long key = pgno;
        while(true) {
            lock.lock();
            if (getting.contains(key)) {
                // 请求的页正在被其他线程获取
                lock.unlock();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            Page page = super.get(key);
            if (page != null) {
                // 请求页已经在缓存中了，直接返回
                return page;
            }

            if (capacity > 0 && count == capacity) {
                // 缓存已满，尝试驱逐
                if (!tryExpelOne()) {
                    lock.unlock();
                    throw Error.CacheFullException;
                }
            }

            count ++;
            getting.add(key);
            lock.unlock();
            break;
        }

        // 从文件获取页
        Page pg;
        try {
            pg = getForCache(key);
        } catch (Exception e) {
            lock.lock();
            count --;
            getting.remove(key);
            lock.unlock();
            throw e;
        }

        // 将请求页添加至页池
        lock.lock();
        add(pg);
        lock.unlock();

        return pg;
    }

    // 释放页
    @Override
    public void release(Page page) {

    }

    @Override
    public void close() {
        lock.lock();
        super.close();
        lock.unlock();
    }

    @Override
    protected Page getForCache(long key) throws Exception {
        return null;
    }

    @Override
    protected void releaseForCache(Page obj) {

    }
}