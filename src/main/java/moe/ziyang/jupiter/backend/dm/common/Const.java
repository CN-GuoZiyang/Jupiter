package moe.ziyang.jupiter.backend.dm.common;

public class Const {

    public static final int PAGE_SIZE = 1 << 13;            // 单页大小
    public static final int BUDDY_BLOCK_SIZE = 1 << 5;      // Buddy Page 单块大小
    public static final int BUDDY_TREE_LEVELS = 9;          // Buddy Tree 层数
    public static final int BUDDY_PAGE_MAX_ALLOCATE_SIZE = 1 << 12; // Buddy Page 最大可单次分配大小
    public static final int PAGE_MAX_ALLOCATE = PAGE_SIZE - 1;      // 最大可单次分配大小
    public static final int HUGE_PAGE_START = 1;            // Huge Page 可分配起始字节

}
