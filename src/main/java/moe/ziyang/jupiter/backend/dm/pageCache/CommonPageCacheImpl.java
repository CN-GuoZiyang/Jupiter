package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.common.LRUCache;
import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.common.Util;
import moe.ziyang.jupiter.backend.dm.fileManager.FileManager;
import moe.ziyang.jupiter.backend.dm.page.CommonPage;
import moe.ziyang.jupiter.common.DBError;

public class CommonPageCacheImpl extends LRUCache<CommonPage> implements CommonPageCache {

    // 文件读写
    private FileManager fm;

    public CommonPageCacheImpl(FileManager fm, int capacity) {
        super(capacity);
        this.fm = fm;
    }

    // 获取页，但是不获取对该页的使用，上层自主加锁
    @Override
    public CommonPage get(int pgno) throws DBError {
        return super.get(pgno);
    }

    @Override
    protected CommonPage getFromSource(long key) throws DBError {
        int pgno = (int)key;
        int offset = Util.pageOffset(pgno);
        return CommonPage.getPageByRaw(pgno, fm.read(offset, Const.PAGE_SIZE));
    }

    @Override
    protected void returnToSource(CommonPage pg) {
        if(pg.isDirty()) {
            flush(pg);
        }
    }

    private void flush(CommonPage pg) {
        int pgno = pg.getPageNumber();
        int offset = Util.pageOffset(pgno);
        fm.write(pg.getData(), offset);
    }

}