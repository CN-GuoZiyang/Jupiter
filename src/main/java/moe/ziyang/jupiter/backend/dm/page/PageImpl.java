package moe.ziyang.jupiter.backend.dm.page;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageImpl implements Page {

    private Lock operateLock;   // 保护变量

    private int pgno;
    private Lock writelock;
    private boolean dirty;      // 是否是脏页
    private int using;

    // 实际存储的数据
    private byte[] data;

    // 被驱逐位
    private boolean expelled;

    public PageImpl(int pgno, byte[] data) {
        this.pgno = pgno;
        this.data = data;
        writelock = new ReentrantLock();
        operateLock = new ReentrantLock();
    }

    @Override
    public long getPageNumber() {
        return pgno;
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
    public boolean canExpel() {
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

    @Override
    public void setDirty(boolean dirty) {
        operateLock.lock();
        this.dirty = dirty;
        operateLock.unlock();
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public byte[] getData() {
        return data;
    }

}
