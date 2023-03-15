package moe.ziyang.jupiter.backend.dm.common;

import java.security.SecureRandom;
import java.util.Random;

public class Util {

    public static long pageOffset(int pgno) {
        return pgno * Const.PAGE_SIZE;
    }

    public static byte[] randomBytes(int length) {
        Random r = new SecureRandom();
        byte[] buf = new byte[length];
        r.nextBytes(buf);
        return buf;
    }
    
}