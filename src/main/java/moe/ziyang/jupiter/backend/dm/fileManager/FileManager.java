package moe.ziyang.jupiter.backend.dm.fileManager;

public interface FileManager {

    int getPageNumber();
    byte[] read(int offset, int length);
    void write(byte[] bytes, int offset);
    int newPage();

}
