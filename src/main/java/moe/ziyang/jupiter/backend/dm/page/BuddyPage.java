package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;
import moe.ziyang.jupiter.common.DBError;

import java.util.ArrayList;
import java.util.List;

import static moe.ziyang.jupiter.common.DBError.PageNoFreeSpaceException;

public class BuddyPage extends CommonPage {

    private BuddyNode root;                 // Buddy Tree 根节点
    private List<BuddyNode> leafNodes;      // Buddy Tree 叶子节点

    public BuddyPage(int pgno) {
        super(pgno, getInitRaw());
        initBuddyTree();
        setDirty();
    }

    public BuddyPage(int pgno, byte[] data) {
        super(pgno, data);
        initBuddyTree();
    }

    public static boolean IsBuddyPage(byte[] raw) {
        // 第一位为 0
        return (raw[0] & 1) == 0;
    }

    // 获取最大可单次分配的块数
    public int getMaxFreeBlock() {
        return root.free;
    }

    private static byte[] getInitRaw() {
        byte[] bytes = new byte[Const.PAGE_SIZE];
        // 第 0 个字节的第一位为 0，无需设置
        // 初始化 Buddy 树，设置初始两个块（存放 Buddy 树）为占用
        bytes[32] |= 0X03;
        return bytes;
    }

    private static class BuddyNode {
        int level;          // 该节点位于第几层（从 0 开始）
        int index;          // 该节点位于该层第几个（从 0 开始）
        int free;           // 该节点对应的区域最大可分配块数
        BuddyNode parent, left, right;

        private BuddyNode(int level, int index) {
            this.level = level;
            this.index = index;
        }

        // 获取该节点对应的区域可用块数
        private int getSpace() {
            return 1 << (Const.BUDDY_TREE_LEVELS-level-1);
        }
    }

    // 根据 raw 初始化 Buddy Tree
    private void initBuddyTree() {
        byte[] data = getData();
        this.root = new BuddyNode(0, 0);
        this.root.free = (data[0] & (1 << 1)) != 0 ? 0 : this.root.getSpace();

        List<BuddyNode> currentLevelNodes = new ArrayList<>();
        List<BuddyNode> nextLevelNodes = new ArrayList<>();
        currentLevelNodes.add(root);
        // 遍历全部有子节点的节点
        for (int level = 0; level < Const.BUDDY_TREE_LEVELS-1; level++) {
            for (int i = 0; i < currentLevelNodes.size(); i++) {
                BuddyNode current = currentLevelNodes.get(i);

                BuddyNode left = new BuddyNode(level + 1, i << 1);
                BuddyNode right = new BuddyNode(level + 1, (i << 1) + 1);

                int currentBitIndex = (1 << level) + i;
                int leftBitIndex = currentBitIndex << 1;
                int rightBitIndex = (currentBitIndex << 1) + 1;

                left.free = (data[leftBitIndex >> 3] & (1 << (leftBitIndex & 0x7))) != 0 ? 0 : left.getSpace();
                right.free = (data[rightBitIndex >> 3] & (1 << (rightBitIndex & 0x7))) != 0 ? 0 : right.getSpace();

                current.left = left;
                current.right = right;
                left.parent = current;
                right.parent = current;

                nextLevelNodes.add(left);
                nextLevelNodes.add(right);
            }
            currentLevelNodes = nextLevelNodes;
            nextLevelNodes = new ArrayList<>();
        }

        leafNodes = new ArrayList<>(currentLevelNodes);

        // 从下到上校准 occupy
        while (currentLevelNodes.size() != 1) {
            // 两个两个读取以校准父节点
            for (int i = 0; i < currentLevelNodes.size() / 2; i+=2) {
                BuddyNode left = currentLevelNodes.get(i);
                BuddyNode right = currentLevelNodes.get(i+1);
                BuddyNode parent = left.parent;

                parent.free = parent.free == 0
                        ? 0
                        : (left.free + right.free == parent.getSpace() ? parent.getSpace() : Math.max(left.free, right.free));

                nextLevelNodes.add(parent);
            }
            currentLevelNodes = nextLevelNodes;
            nextLevelNodes = new ArrayList<>();
        }
    }

    // 使用 Buddy Tree 分配 block 长度的空间
    // block 应为 2 的幂
    // 返回页内偏移
    private int allocateBlock(int block) throws DBError {
        if (root.free < block) {
            throw PageNoFreeSpaceException;
        }

        // 定位到恰好满足的节点
        BuddyNode current = root;
        while (current.free != block) {
            int left = current.left.free;
            int right = current.right.free;
            // 优先选择最小的满足条件的分叉
            if (left <= right) {
                current = left >= block ? current.left : current.right;
            } else {
                current = right >= block ? current.right : current.left;
            }
        }

        // 对应位设置为 1
        int bitIndex = (1 << current.level) + current.index;
        data[bitIndex >> 3] |= (1 << (bitIndex & 0x7));
        // 该块起始的页内偏移
        int offset = (1 << (8 - current.level)) * current.index;

        // 向上修改父节点的可分配值
        current = current.parent;
        while (current != null) {
            current.free = Math.max(current.left.free, current.right.free);
            current = current.parent;
        }

        setDirty();
        return offset;
    }

    // 给定页内偏移，释放块
    private void freeBlock(int offset) {
        // 偏移对应的叶子节点
        BuddyNode current = leafNodes.get(offset / Const.BUDDY_BLOCK_SIZE);

        // 向上找到第一个可用块为 0 的节点
        while (current.free != 0) {
            current = current.parent;
        }

        // 修改节点为未使用
        current.free = current.getSpace();
        int bitIndex = (1 << current.level) + current.index;
        data[bitIndex >> 3] &= ~(1 << (bitIndex & 0x7));

        // 继续向上回溯，合并连续空闲空间
        current = current.parent;
        while (current != null) {
            BuddyNode left = current.left;
            BuddyNode right = current.right;
            current.free = left.free + right.free == current.getSpace()
                    ? current.getSpace()
                    : Math.max(left.free, right.free);
            current = current.parent;
        }
    }

    @Override
    public int allocate(int size) {
        int block = size / Const.BUDDY_BLOCK_SIZE;
        // from Hashmap
        int n = block - 1;
        n |= n >>> 1;
        n |= n >>> 2;
        n |= n >>> 4;
        n |= n >>> 8;
        n |= n >>> 16;
        block = n < 0 ? 1 : n + 1;
        return allocateBlock(block);
    }

    @Override
    public void free(int offset) {
        freeBlock(offset);
    }

}
