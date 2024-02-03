package traversal;

import model.PuzzleInfo;
import paths.PathRadixTree;
import paths.Shortcut;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BruteForceShortcutHunter extends ShortcutHunter {

    public BruteForceShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap) {
        super(puzzleInfo, maxDepth, pathMap);
    }

    public BruteForceShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, boolean printShortcuts) {
        super(puzzleInfo, maxDepth, pathMap, printShortcuts);
    }

    @Override
    public List<Shortcut> performSearch() {
        System.out.println("Starting brute force shortcut search.");
        return IntStream.range(0, allowedMoves.length).boxed().parallel()
                .flatMap(index -> new BruteForceShortcutHunter(puzzleInfo, maxDepth, pathMap, printShortcuts).searchWithMove(index))
                .toList();
    }

    public Stream<Shortcut> searchWithMove(int index) {
        this.transform(allowedMoves[index]);
        moveIndex = 0;
        moveIndexes.push(index);
        checkForShortcuts();
        while (performStep());
        this.transform(allowedMoves[moveIndexes.pop()].getInverse());
        System.out.println("Done with search " + index);
        return this.foundShortcuts.stream();
    }

    protected boolean performStep() {
        while (moveIndex == allowedMoves.length) {
            if (moveIndexes.size() <= 2) {
                return false;
            } else {
                moveIndex = moveIndexes.pop();
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
            }
        }
        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
            this.transform(allowedMoves[moveIndex]);
            moveIndexes.push(moveIndex);
            moveIndex = 0;
            checkForShortcuts();
            if (moveIndexes.size() >= maxDepth) {
                moveIndex = moveIndexes.pop();
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
            }
        } else {
            ++moveIndex;
        }
        return true;
    }
}
