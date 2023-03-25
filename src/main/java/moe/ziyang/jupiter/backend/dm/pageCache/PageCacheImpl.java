package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.common.LRUCache;
import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.common.Util;
import moe.ziyang.jupiter.backend.dm.page.Page;
import moe.ziyang.jupiter.backend.dm.page.PageImpl;
import moe.ziyang.jupiter.common.DBError;
import moe.ziyang.jupiter.common.Panic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageCacheImpl extends LRUCache<Page> implements PageCache {

    // db 文件流
    private FileChannel fc;
    // 文件锁
    private Lock fileLock;

    // db 文件的总页数
    private AtomicInteger pageNumbers;

    public PageCacheImpl(RandomAccessFile file, FileChannel fileChannel, int capacity) {
        super(capacity);
        long length = 0;
        try {
            length = file.length();
        } catch (IOException e) {
            Panic.panic(e);
        }
        this.fc = fileChannel;
        this.fileLock = new ReentrantLock();
        this.pageNumbers = new AtomicInteger((int)length / Const.PAGE_SIZE);
    }

    @Override
    public int getPageNumber() {
        return pageNumbers.get();
    }

    // 获取页，但是不获取对该页的使用，上层自主加锁
    @Override
    public Page get(int pgno) throws DBError {
        return super.get(pgno);
    }

    @Override
    protected Page getFromSource(long key) throws DBError {
        int pgno = (int)key;
        long offset = Util.pageOffset(pgno);

        ByteBuffer buf = ByteBuffer.allocate(Const.PAGE_SIZE);
        fileLock.lock();
        try {
            fc.position(offset);
            fc.read(buf);
        } catch(IOException e) {
            Panic.panic(e);
        }
        fileLock.unlock();
        return new PageImpl(pgno, buf.array());
    }

    @Override
    protected void returnToSource(Page pg) {
        if(pg.isDirty()) {
            flush(pg);
        }
    }

    private void flush(Page pg) {
        int pgno = pg.getPageNumber();
        long offset = Util.pageOffset(pgno);

        fileLock.lock();
        try {
            ByteBuffer buf = ByteBuffer.wrap(pg.getData());
            fc.position(offset);
            fc.write(buf);
            fc.force(false);
        } catch(IOException e) {
            Panic.panic(e);
        } finally {
            fileLock.unlock();
        }
    }

}