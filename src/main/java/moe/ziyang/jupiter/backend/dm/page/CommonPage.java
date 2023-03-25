package moe.ziyang.jupiter.backend.dm.page;

// 可用于分配、实际存储内容的页
public abstract class CommonPage extends PageImpl {

    public CommonPage(int pgno, byte[] data) {
        super(pgno, data);
    }

    // 根据字节序列建立对应的可分配页实体
    public static CommonPage getPageByRaw(int pgno, byte[] raw) {
        if (BuddyPage.IsBuddyPage(raw)) {
            return new BuddyPage(pgno, raw);
        }
        return new HugePage(pgno, raw);
    }

    // 根据给定字节大小分配空间，返回分配空间起始位置偏移
    public abstract int allocate(int size);
    // 释放以 offset 为起始的空间
    public abstract void free(int offset);

}
