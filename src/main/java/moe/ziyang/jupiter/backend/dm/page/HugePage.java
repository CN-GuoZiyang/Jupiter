package moe.ziyang.jupiter.backend.dm.page;

public class HugePage extends PageAllocatable {
    public HugePage(int pgno, byte[] data) {
        super(pgno, data);
    }

    public static boolean IsHugePage(byte[] raw) {
        return (raw[0] & (1 << 7)) != 0;
    }
}
