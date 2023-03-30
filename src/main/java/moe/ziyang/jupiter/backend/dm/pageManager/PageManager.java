package moe.ziyang.jupiter.backend.dm.pageManager;

import moe.ziyang.jupiter.backend.dm.common.UniqID;

public interface PageManager {

    UniqID insert(byte[] bytes);

}
