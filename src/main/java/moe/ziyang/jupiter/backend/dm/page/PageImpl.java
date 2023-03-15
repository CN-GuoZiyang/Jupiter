package moe.ziyang.jupiter.backend.dm.page;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageImpl implements Page {

    private Lock operateLock;   // 保护变量

    private int pgno;
    private Lock writelock;
    private int using;

    // 被驱逐位
    private boolean expelled;

    public PageImpl(int pgno) {
        this.pgno = pgno;
        writelock = new ReentrantLock();
        operateLock = new ReentrantLock();
    }

    @Override
    public long getKey() {
        return pgno;
    }

    // 读写非元信息字段时加读锁
    @Override
    public boolean readLock() {
        operateLock.lock();
        if (expelled) {
            return false;
        }
        using ++;
        operateLock.unlock();
        return true;
    }

    @Override
    public void readUnlock() {
        operateLock.lock();
        using --;
        operateLock.unlock();
    }

    // 读写元信息字段时加写锁
    @Override
    public boolean writeLock() {
        operateLock.lock();
        if (expelled) {
            return false;
        }
        operateLock.unlock();
        writelock.lock();
        return true;
    }

    @Override
    public void writeUnlock() {
        writelock.unlock();
    }

    @Override
    public boolean canRelease() {
        operateLock.lock();
        try {
            if (using != 0) {
                return false;
            }
            if (!writelock.tryLock()) {
                return false;
            }
            expelled = true;
            return true;
        } finally {
            operateLock.unlock();
        }
    }
}
