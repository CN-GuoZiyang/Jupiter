package moe.ziyang.jupiter.common;

public class Error {
    // common
    public static final Exception CacheFullException = new RuntimeException("Cache is full!");
    public static final Exception NoFreePageException = new RuntimeException("No free page found!");
}
