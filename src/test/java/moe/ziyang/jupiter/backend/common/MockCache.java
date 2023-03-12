package moe.ziyang.jupiter.backend.common;

public class MockCache extends AbstractCache<MockObj> {

    public MockCache() {
        super(50);
    }

    @Override
    protected MockObj getForCache(long key) {
        MockObj obj = new MockObj();
        obj.l = key;
        return obj;
    }

    @Override
    protected void releaseForCache(MockObj obj) {

    }


}

class MockObj implements Cacheable {
    Long l;

    @Override
    public void readLock() {

    }

    @Override
    public void writeLock() {

    }

    @Override
    public boolean tryLock() {
        return true;
    }

    @Override
    public void setExpel() {

    }
}