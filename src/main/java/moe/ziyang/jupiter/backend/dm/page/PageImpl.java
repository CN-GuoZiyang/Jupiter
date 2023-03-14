package moe.ziyang.jupiter.backend.dm.page;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PageImpl implements Page {

    private int pgno;
    private Lock lock;
    private AtomicInteger using;

    // 被驱逐位
    private boolean expelled;

    public PageImpl(int pgno) {
        this.pgno = pgno;
        lock = new ReentrantLock();
        using = new AtomicInteger();
    }

    @Override
    public long getKey() {
        return pgno;
    }

    // 读写非元信息字段时加读锁
    @Override
    public void readLock() {
        using.incrementAndGet();
    }

    // 读写元信息字段时加写锁
    @Override
    public void writeLock() {
        using.incrementAndGet();
        lock.lock();
    }

    @Override
    public boolean canRelease() {
        using.incrementAndGet();
        if (!lock.tryLock()) {
            using.decrementAndGet();
            return false;
        }
        return true;
    }

    @Override
    public void setExpel() {
        expelled = true;
    }
}
