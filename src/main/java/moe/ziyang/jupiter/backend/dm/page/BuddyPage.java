package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;

public class BuddyPage extends PageAllocatable {

    public BuddyPage(int pgno) {
        super(pgno, getInitRaw());
    }

    public BuddyPage(int pgno, byte[] data) {
        super(pgno, data);
    }

    public static boolean IsBuddyPage(byte[] raw) {
        // 第一位为 0
        return (raw[0] & (1 << 7)) == 0;
    }

    public static byte[] getInitRaw() {
        byte[] bytes = new byte[Const.PAGE_SIZE];
        // 第 0 个字节的第一位为 0，无需设置
        // 初始化 Buddy 树
        
        return bytes;
    }

}
