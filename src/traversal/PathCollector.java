package traversal;

import paths.MoveNode;
import paths.Path;
import paths.PathRadixTree;
import model.*;

import java.util.*;

public class PathCollector extends Mutator {
    private final Puzzle puzzle;

    private final int minLength; // Should be at least ShortcutHunter.maxDepth + 1

    private final int maxLength;
    private final Map<Long, PathRadixTree> pathMap;

    public final static Map<Long, PathRadixTree> DEFAULT_PATH_MAP = new HashMap<>();


    public PathCollector(Puzzle puzzle, Map<Long, PathRadixTree> pathMap, int maxLength, int minLength) {
        super(puzzle.getInitialState());
        this.puzzle = puzzle;
        this.maxLength = maxLength;
        this.minLength = minLength;
        this.pathMap = pathMap;
    }

    public void collectPaths() {
        MoveNode firstNode = puzzle.getSolution();
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
                        pathMap.put(gameHash, new PathRadixTree(positions, path));
                    }
                }
            }
            firstNode = firstNode.getNext();
        }
        System.out.println("Gathered " + pathCount + " paths.");
    }
}
