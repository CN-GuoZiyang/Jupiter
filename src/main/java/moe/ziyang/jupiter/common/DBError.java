package moe.ziyang.jupiter.common;

public class DBError extends RuntimeException {

    public DBError() {
        super();
    }

    public DBError(String message, Throwable cause) {
        super(message, cause);
    }

    public DBError(String msg) {
        super(msg);
    }

    public DBError(Throwable cause) {
        super(cause);
    }

    // common
    public static final DBError CacheFullException = new DBError("Cache is full!");
    public static final DBError NoFreePageException = new DBError("No free page found!");
    public static final DBError PageNoFreeSpaceException = new DBError("Page has no free space!");
    public static final DBError DataTooLargeException = new DBError("Insert data too large!");
}
