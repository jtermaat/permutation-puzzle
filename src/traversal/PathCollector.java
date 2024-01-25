package traversal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.*;

import java.util.*;

@AllArgsConstructor
@Data
public class PathCollector extends Permutation {
    private Puzzle puzzle;
    private Move[] allowedMoves;
    private int maxDepth;
    public PathCollector(Puzzle puzzle, PuzzleInfo puzzleInfo, int maxDepth) {
        super(puzzle.getInitialState());
        this.puzzle = puzzle;
        this.allowedMoves = puzzleInfo.getAllowedMoves();
        this.maxDepth = maxDepth;
    }

    protected void resetPositions() {
        for (int i = 0;i<positions.length;++i) {
            positions[i] = i;
        }
    }

    public void collectPaths(MoveNode firstNode, Map<Long, Map<Permutation, List<Path>>> pathMap) {
        while (firstNode != null) {
            this.resetPositions();
            this.calculateGameHash();
            int length = 0;
            MoveNode secondNode = firstNode;
            while (secondNode != null) {
                this.transform(secondNode.getMove());
                secondNode = secondNode.getNext();
                ++ length;
                if (length > maxDepth) {
                    int[] thesePositions = new int[positions.length];
                    System.arraycopy(positions, 0, thesePositions, 0, positions.length);
                    final Path path = new Path(firstNode, secondNode, thesePositions);
                    pathMap.putIfAbsent(gameHash, new HashMap<>());
                    final Map<Permutation, List<Path>> equalityMap = pathMap.get(gameHash);
                    equalityMap.putIfAbsent(this, new ArrayList<>());
                    equalityMap.get(this).add(path);
                }
            }
            firstNode = firstNode.getNext();
        }
    }
}
