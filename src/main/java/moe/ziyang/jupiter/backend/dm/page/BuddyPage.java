package moe.ziyang.jupiter.backend.dm.page;

public class BuddyPage extends PageAllocatable {
    public BuddyPage(int pgno, byte[] data) {
        super(pgno, data);
    }

    public static boolean IsBuddyPage(byte[] raw) {
        return (raw[0] & (1 << 7)) == 0;
    }
}
