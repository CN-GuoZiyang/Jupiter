package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;

public class HugePage extends CommonPage {
    public HugePage(int pgno, byte[] data) {
        super(pgno, data);
    }

    public static boolean IsHugePage(byte[] raw) {
        // 第一位为 1
        return (raw[0] & 1) == 1;
    }

    @Override
    public int allocate(int size) {
        return Const.HUGE_PAGE_START;
    }

    @Override
    public void free(int offset) {
        // 无操作，Huge Page 释放内存直接将页标记为未使用即可
    }
}
