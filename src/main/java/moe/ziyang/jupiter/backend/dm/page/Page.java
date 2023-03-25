package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.common.Cacheable;

public interface Page extends Cacheable {

    // 获取页号
    int getPageNumber();
    // 设置为脏页
    void setDirty();
    // 页是否是脏页，线程不安全
    boolean isDirty();
    // 获取页内数据
    byte[] getData();

}
