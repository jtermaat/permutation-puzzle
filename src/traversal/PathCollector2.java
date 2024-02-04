package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.MoveNode;
import paths.Path;
import paths.PathRadixTree2;

public class PathCollector2 extends Mutator {

    private final int minLength; // Should be at least ShortcutHunter.maxDepth + 1
    private final int maxLength;

    @Getter
    @Setter
    private PathRadixTree2 pathTree;
    private final MoveNode node;

    public final static PathRadixTree2 DEFAULT_PATH_TREE = new PathRadixTree2();


    public PathCollector2(MoveNode node, PathRadixTree2 pathTree, int maxLength, int minLength) {
        super(node.getMove().getNewPositions().length);
        this.node = node;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.pathTree = pathTree;
    }

    public PathCollector2(MoveNode node, int maxLength, int minLength) {
        this(node, DEFAULT_PATH_TREE, maxLength, minLength);
    }

    public int collectPaths() {
        MoveNode firstNode = node;
        int pathCount = 0;
        int count = 0;
        while (firstNode != null && count < maxLength) {
            ++count;
            this.resetPositions();
            int length = 0;
            MoveNode secondNode = firstNode;
            while (secondNode != null && length < maxLength) {
                this.transform(secondNode.getMove());
                secondNode = secondNode.getNext();
                ++ length;
                if (length > minLength) {
                    ++pathCount;
                    final Path path = new Path(firstNode, secondNode);
                    pathTree.put(positions, path);
                }
            }
            firstNode = firstNode.getNext();
        }
        return pathCount;
    }
}