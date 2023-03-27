package moe.ziyang.jupiter.backend.dm.pageManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.fileManager.FileManager;
import moe.ziyang.jupiter.backend.dm.page.*;
import moe.ziyang.jupiter.backend.dm.pageCache.CommonPageCache;
import moe.ziyang.jupiter.backend.dm.pageCache.CommonPageCacheImpl;

public class PageManagerImpl implements PageManager {

    private static Map<Integer, Integer> indexMap = Map.of(
            Const.BUDDY_BLOCK_SIZE * 256, 8,
            Const.BUDDY_BLOCK_SIZE * 128, 7,
            Const.BUDDY_BLOCK_SIZE * 64, 6,
            Const.BUDDY_BLOCK_SIZE * 32, 5,
            Const.BUDDY_BLOCK_SIZE * 16, 4,
            Const.BUDDY_BLOCK_SIZE * 8, 3,
            Const.BUDDY_BLOCK_SIZE * 4, 2,
            Const.BUDDY_BLOCK_SIZE * 2, 1,
            Const.BUDDY_BLOCK_SIZE, 0
    );
    
    private CommonPageCache pc;
    private FileManager fm;
    private List<Integer>[] buddyPageIndex;

    private PageZero pg0;
    private PageFree pgf;

    @SuppressWarnings("unchecked")
    public PageManagerImpl(FileManager fm, int capacity) {
        this.fm = fm;
        this.pc = new CommonPageCacheImpl(fm, capacity);
        this.buddyPageIndex = new List[9];
        this.pg0 = new PageZero(fm.read(0, Const.PAGE_SIZE));
        this.pgf = new PageFree(fm.read(Const.PAGE_SIZE, Const.PAGE_SIZE));
        initIndex();
    }

    private void initIndex() {
        int pgno = fm.getPageNumber();
        for (int i = 0; i < 9; i++) {
            buddyPageIndex[i] = new ArrayList<>();
        }
        for (int i = 2; i <= pgno; i ++) {
            if (pgf.checkIsFreePage(i)) {
                continue;
            }
            CommonPage pg = pc.get(i);
            // 将页面插入 index
            insert(pg);
            pc.release(pg, true);
        }
    }

    private void insert(CommonPage page) {
        if (page instanceof HugePage) {
            return;
        }
        BuddyPage bPage = (BuddyPage) page;
        int free = bPage.getMaxFreeBlock() * Const.BUDDY_BLOCK_SIZE;
        buddyPageIndex[indexMap.get(free)].add(bPage.getPageNumber());
    }

}
