package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.common.DBError;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class PageFree extends PageImpl {

    private Lock writeLock;
    public static final int PAGE_FREE_PGNO = 1;

    // 从已有数据创建空闲页
    public PageFree(byte[] data) {
        super(PAGE_FREE_PGNO, data);
        writeLock = new ReentrantLock();
    }

    // 从零创建
    public PageFree() {
        super(PAGE_FREE_PGNO, new byte[Const.PAGE_SIZE]);
        initBytes();
    }

    private void initBytes() {
        setDirty();
        // 空闲页初始化只需要将第一个字节的最高两位为1
        data[0] = (byte) (1 << 7 | 1 << 6);
    }

    // 将某页设置为空闲
    public void setPageFree(int pgno) {
        writeLock.lock();
        try {
            int byteNum = pgno / 8;
            int bitNum = pgno % 8;
            setDirty();
            data[byteNum] &= ~(1 << (7 - bitNum));
        } finally {
            writeLock.unlock();
        }
    }

    // 获取一个空闲页号，并占用
    public int getFreePage() throws Exception {
        writeLock.lock();
        try {
            for (int i = 2; i < Const.PAGE_SIZE; i ++) {
                if (data[i] != (byte) 0xFF) {
                    // 某字节未满，判断最高的零
                    for (int j = 0; j < 8; j ++) {
                        if ((data[i] & (1 << (7-j))) == 0) {
                            setDirty();
                            data[i] |= 1 << (7-j);
                            return i*8+j;
                        }
                    }
                }
            }
        } finally {
            writeLock.unlock();
        }
        throw DBError.NoFreePageException;
    }

}
