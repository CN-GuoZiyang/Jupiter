package moe.ziyang.jupiter.backend.dm.fileManager;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.backend.dm.page.CommonPage;
import moe.ziyang.jupiter.common.Panic;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class FileManagerImpl implements FileManager {

    private RandomAccessFile file;
    private FileChannel fc;
    // db 文件的总页数
    private int pageNumbers;

    public FileManagerImpl(RandomAccessFile file, FileChannel fileChannel) {
        this.file = file;
        this.fc = fileChannel;
        long length = 0;
        try {
            length = file.length();
        } catch (IOException e) {
            Panic.panic(e);
        }
        this.pageNumbers = (int)length / Const.PAGE_SIZE;
    }


    @Override
    public int getPageNumber() {
        return pageNumbers;
    }

    @Override
    public synchronized byte[] read(int offset, int length) {
        ByteBuffer buf = ByteBuffer.allocate(Const.PAGE_SIZE);
        try {
            fc.position(offset);
            fc.read(buf);
        } catch(IOException e) {
            Panic.panic(e);
        }
        return buf.array();
    }

    @Override
    public synchronized void write(byte[] bytes, int offset) {
        try {
            ByteBuffer buf = ByteBuffer.wrap(bytes);
            fc.position(offset);
            fc.write(buf);
            fc.force(false);
        } catch(IOException e) {
            Panic.panic(e);
        }
        if (this.pageNumbers < offset/Const.PAGE_SIZE+1) {
            this.pageNumbers = offset/Const.PAGE_SIZE+1;
        }
    }

    @Override
    public synchronized int newPage() {
        return ++this.pageNumbers;
    }

}
