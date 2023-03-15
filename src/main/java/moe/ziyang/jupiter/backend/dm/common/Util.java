package moe.ziyang.jupiter.backend.dm.common;

public class Util {

    public static long pageOffset(int pgno) {
        return pgno * Const.PAGE_SIZE;
    }
    
}