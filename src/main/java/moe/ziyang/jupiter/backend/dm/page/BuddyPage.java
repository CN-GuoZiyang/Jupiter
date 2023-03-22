package moe.ziyang.jupiter.backend.dm.page;

import moe.ziyang.jupiter.backend.dm.common.Const;

import java.util.ArrayList;
import java.util.List;

public class BuddyPage extends PageAllocatable {

    private BuddyNode root;                 // Buddy Tree 根节点
    private List<BuddyNode> leafNodes;      // Buddy Tree 叶子节点

    public BuddyPage(int pgno) {
        super(pgno, getInitRaw());
        initBuddyTree();
    }

    public BuddyPage(int pgno, byte[] data) {
        super(pgno, data);
        initBuddyTree();
    }

    public static boolean IsBuddyPage(byte[] raw) {
        // 第一位为 0
        return (raw[0] & 1) == 0;
    }

    public static byte[] getInitRaw() {
        byte[] bytes = new byte[Const.PAGE_SIZE];
        // 第 0 个字节的第一位为 0，无需设置
        // 初始化 Buddy 树，设置初始两个块（存放 Buddy 树）为占用
        bytes[32] |= 0X03;
        return bytes;
    }

    private class BuddyNode {
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

                parent.free = parent.free == 0 ? 0 : left.free + right.free;

                nextLevelNodes.add(parent);
            }
            currentLevelNodes = nextLevelNodes;
            nextLevelNodes = new ArrayList<>();
        }
    }

}
