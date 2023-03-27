package moe.ziyang.jupiter.backend.dm.pageCache;

import moe.ziyang.jupiter.backend.common.CachePool;
import moe.ziyang.jupiter.backend.dm.page.CommonPage;
import moe.ziyang.jupiter.common.DBError;

public interface CommonPageCache extends CachePool<CommonPage> {

    CommonPage get(int pgno) throws DBError;

}
