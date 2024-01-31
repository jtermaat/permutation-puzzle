package traversal;

import paths.MoveNode;
import paths.Path;
import paths.PathRadixTree;
import model.*;

import java.util.*;

public class PathCollector extends Permutation {
    private Puzzle puzzle;
    private int maxDepth;
    private final Map<Long, PathRadixTree> pathMap;

    private final static int MAX_LENGTH = 30; //Integer.MAX_VALUE; //Integer.MAX_VALUE;  //15;

    private final static int MAX_COUNT = Integer.MAX_VALUE;

    public PathCollector(Puzzle puzzle, PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap) {
        super(puzzle.getInitialState());
        this.puzzle = puzzle;
//        this.allowedMoves = puzzleInfo.getAllowedMoves();
        this.maxDepth = maxDepth;
        this.pathMap = pathMap;
    }

    public void collectPaths() {
        MoveNode firstNode = puzzle.getSolution();
        int pathCount = 0;
        int count = 0;
        while (firstNode != null && count < MAX_COUNT) {
            ++count;
            this.resetPositions();
            int length = 0;
            MoveNode secondNode = firstNode;
            while (secondNode != null && length < MAX_LENGTH) {
                this.transform(secondNode.getMove());
                secondNode = secondNode.getNext();
                ++ length;
                if (length > maxDepth) {
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
