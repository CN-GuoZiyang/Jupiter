package moe.ziyang.jupiter.backend.dm.pageManager;

import java.util.ArrayList;
import java.util.List;

import moe.ziyang.jupiter.backend.dm.page.CommonPage;
import moe.ziyang.jupiter.backend.dm.page.Page;
import moe.ziyang.jupiter.backend.dm.pageCache.PageCache;

public class PageManagerImpl implements PageManager {
    
    private PageCache pc;
    private List<Integer>[] pageIndex;

    @SuppressWarnings("unchecked")
    public PageManagerImpl(PageCache pc) {
        this.pc = pc;
        this.pageIndex = new List[9];
        initIndex();
    }

    private void initIndex() {
        int pgno = pc.getPageNumber();
        for (int i = 2; i <= pgno; i ++) {
            Page pg = pc.get(i);
        }
    }

    private void insert(CommonPage page) {
        
    }

}
