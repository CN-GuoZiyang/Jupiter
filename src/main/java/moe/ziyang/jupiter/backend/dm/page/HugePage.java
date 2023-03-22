package moe.ziyang.jupiter.backend.dm.page;

public class HugePage extends PageAllocatable {
    public HugePage(int pgno, byte[] data) {
        super(pgno, data);
    }

    public static boolean IsHugePage(byte[] raw) {
        // 第一位为 1
        return (raw[0] & 1) == 1;
    }
}
