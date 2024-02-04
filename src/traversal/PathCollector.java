package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.MoveNode;
import paths.Path;
import paths.PathRadixTree;

public class PathCollector extends Mutator {

    private final int minLength; // Should be at least ShortcutHunter.maxDepth + 1
    private final int maxLength;

    @Getter
    @Setter
    private PathRadixTree pathTree;
    private final MoveNode node;

    public final static PathRadixTree DEFAULT_PATH_TREE = new PathRadixTree();


    public PathCollector(MoveNode node, PathRadixTree pathTree, int maxLength, int minLength) {
        super(node.getMove().getNewPositions().length);
        this.node = node;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.pathTree = pathTree;
    }

    public int collectPaths() {
        MoveNode firstNode = node;
        int pathCount = 0;
        while (firstNode != null) {
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