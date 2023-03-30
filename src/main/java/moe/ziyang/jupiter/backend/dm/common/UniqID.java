package moe.ziyang.jupiter.backend.dm.common;

public class UniqID {

    private int pgno;
    private int offset;

    public UniqID(int pgno, int offset) {
        this.pgno = pgno;
        this.offset = offset;
    }

    public int getPgno() {
        return pgno;
    }

    public int getOffset() {
        return offset;
    }

}
