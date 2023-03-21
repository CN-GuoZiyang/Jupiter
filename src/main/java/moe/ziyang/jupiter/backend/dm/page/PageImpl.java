package moe.ziyang.jupiter.backend.dm.page;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageImpl implements Page {

    private Lock operateLock;   // 保护变量

    private int pgno;
    private Lock metadataLock;
    private boolean dirty;      // 是否是脏页
    private int using;

    // 实际存储的数据
    protected byte[] data;

    public PageImpl(int pgno, byte[] data) {
        this.pgno = pgno;
        this.data = data;
        metadataLock = new ReentrantLock();
        operateLock = new ReentrantLock();
    }

    @Override
    public int getPageNumber() {
        return pgno;
    }

    @Override
    public long getKey() {
        return getPageNumber();
    }

    @Override
    public boolean canExpel() {
        operateLock.lock();
        if (using != 0) {
            operateLock.unlock();
            return false;
        }
        if (!metadataLock.tryLock()) {
            operateLock.unlock();
            return false;
        }
        return true;
    }

    @Override
    public void setDirty() {
        if (this.dirty) {
            return;
        }
        operateLock.lock();
        this.dirty = true;
        operateLock.unlock();
    }

    @Override
    public boolean isDirty() {
        if (this.dirty) {
            return this.dirty;
        }
        operateLock.lock();
        try {
            return this.dirty;
        } finally {
            operateLock.unlock();
        }
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public void acquireUsage() {
        operateLock.lock();
        using ++;
        operateLock.unlock();
    }

    @Override
    public void returnUsage() {
        operateLock.lock();
        using --;
        operateLock.unlock();
    }

}
