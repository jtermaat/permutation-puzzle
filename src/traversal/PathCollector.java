package traversal;

import paths.MoveNode;
import paths.Path;
import paths.PathRadixTree2;

import java.util.*;

public class PathCollector extends Mutator {

    private final int minLength; // Should be at least ShortcutHunter.maxDepth + 1

    private final int maxLength;
    private final Map<Long, PathRadixTree2> pathMap;
    private final MoveNode node;

    public final static Map<Long, PathRadixTree2> DEFAULT_PATH_MAP = new HashMap<>();


    public PathCollector(MoveNode node, Map<Long, PathRadixTree2> pathMap, int maxLength, int minLength) {
        super(node.getMove().getNewPositions().length);
        this.node = node;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.pathMap = pathMap;
    }

    public PathCollector(MoveNode node, int maxLength, int minLength) {
        this(node, DEFAULT_PATH_MAP, maxLength, minLength);
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
                    if (pathMap.containsKey(gameHash)) {
                        pathMap.get(gameHash).put(positions, path);
                    } else {
                        pathMap.put(gameHash, new PathRadixTree2(positions, path));
                    }
                }
            }
            firstNode = firstNode.getNext();
        }
        return pathCount;
    }
}
