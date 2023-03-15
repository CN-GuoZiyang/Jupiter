package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.common.LRUCache;
import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.common.Util;
import moe.ziyang.jupiter.backend.dm.page.Page;
import moe.ziyang.jupiter.backend.dm.page.PageImpl;
import moe.ziyang.jupiter.common.Error;
import moe.ziyang.jupiter.common.Panic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageCacheImpl extends LRUCache<Page> implements PageCache {

    // 正在获取的资源
    private final Set<Long> getting;
    // 页池锁
    private Lock lock;
    // db 文件
    private RandomAccessFile file;
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
        this.getting = new HashSet<>();
        this.file = file;
        this.fc = fileChannel;
        this.fileLock = new ReentrantLock();
        this.lock = new ReentrantLock();
        this.pageNumbers = new AtomicInteger((int)length / Const.PAGE_SIZE);
    }

    // 获取页，但是不获取对该页的使用，上层自主加锁
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
                lock.unlock();
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

    // 无实现，线程自主解锁即可
    @Override
    public void release(Page page) {}

    @Override
    public void close() {
        lock.lock();
        super.close();
        lock.unlock();
    }

    @Override
    protected Page getForCache(long key) throws Exception {
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
    protected void releaseForCache(Page pg) {
        if(pg.isDirty()) {
            flush(pg);
            pg.setDirty(false);
        }
    }

    private void flush(Page pg) {
        int pgno = (int)pg.getPageNumber();
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