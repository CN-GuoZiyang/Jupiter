package moe.ziyang.jupiter.backend.dm.page;

// 可用于分配、实际存储内容的页
public abstract class PageAllocatable extends PageImpl {

    public PageAllocatable(int pgno, byte[] data) {
        super(pgno, data);
    }

    // 根据字节序列建立对应的可分配页实体
    public static PageAllocatable getPageByRaw(int pgno, byte[] raw) {
        if (BuddyPage.IsBuddyPage(raw)) {
            return new BuddyPage(pgno, raw);
        }
        return new HugePage(pgno, raw);
    }

}
