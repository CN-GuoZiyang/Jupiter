package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.dm.page.Page;
import moe.ziyang.jupiter.common.DBError;

public interface PageCache {

    Page get(int pgno) throws DBError;

}
