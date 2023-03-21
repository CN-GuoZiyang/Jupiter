package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.common.Util;

import java.util.Arrays;

// PageZero
// 第零页，在启动时做 Valid Check
// MAGIC NUMBER
// 启动时在 100~107 字节处写入随机字节，正确关闭时拷贝到 108~115字节
public class PageZero extends PageImpl {

    public static final int PAGE_ZERO_PGNO = 0;
    private static final int INIT_VC_OFFSET = 100;
    private static final int VC_LENGTH = 8;
    private static final int CLOSE_VC_OFFSET = INIT_VC_OFFSET + VC_LENGTH;

    // 从零创建
    public PageZero() {
        super(PAGE_ZERO_PGNO, new byte[Const.PAGE_SIZE]);
        initBytes();
    }

    private void initBytes() {
        // TODO Magic Number

        // Valid Number
        setInitVC();
    }

    // 从已有数据创建
    // 需要手动调用 checkVC 和 setInitVC
    public PageZero(byte[] data) {
        super(PAGE_ZERO_PGNO, data);
        // TODO Magic Check
    }

    private void setInitVC() {
        setDirty();
        System.arraycopy(Util.randomBytes(VC_LENGTH), 0, data, INIT_VC_OFFSET, VC_LENGTH);
    }

    public void setCloseVC() {
        setDirty();
        System.arraycopy(data, INIT_VC_OFFSET, data, CLOSE_VC_OFFSET, VC_LENGTH);
    }

    public boolean checkVC() {
        byte[] initVC = Arrays.copyOfRange(data, INIT_VC_OFFSET, CLOSE_VC_OFFSET);
        byte[] closeVC = Arrays.copyOfRange(data, CLOSE_VC_OFFSET, CLOSE_VC_OFFSET+VC_LENGTH);
        return Arrays.equals(initVC, closeVC);
    }
}
