package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.common.Error;

public class PageFree extends PageImpl {

    public static final int PAGE_FREE_PGNO = 1;

    // 从已有数据创建空闲页
    public PageFree(byte[] data) {
        super(PAGE_FREE_PGNO, data);
    }

    // 从零创建
    public PageFree() {
        super(PAGE_FREE_PGNO, new byte[Const.PAGE_SIZE]);
        initBytes();
    }

    private void initBytes() {
        setDirty(true);
        // 空闲页初始化只需要将第一个字节的最高两位为1
        data[0] = (byte) (1 << 7 | 1 << 6);
    }

    // 将某页设置为空闲
    public void setPageFree(int pgno) {
        writeLock();
        int byteNum = pgno / 8;
        int bitNum = pgno % 8;
        setDirty(true);
        data[byteNum] &= ~(1 << (7-bitNum));
        writeUnlock();
    }

    // 获取一个空闲页号，并占用
    public int getFreePage() throws Exception {
        writeLock();
        for (int i = 2; i < Const.PAGE_SIZE; i ++) {
            if (data[i] != (byte) 0xFF) {
                // 某字节未满，判断最高的零
                for (int j = 0; j < 8; j ++) {
                    if ((data[i] & (1 << (7-j))) == 0) {
                        setDirty(true);
                        data[i] |= 1 << (7-j);
                        writeUnlock();
                        return i*8+j;
                    }
                }
            }
        }
        writeUnlock();
        throw Error.NoFreePageException;
    }

}
