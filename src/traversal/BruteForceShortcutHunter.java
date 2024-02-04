package traversal;

import model.Move;
import model.PuzzleInfo;
import paths.PathRadixTree;
import paths.Shortcut;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BruteForceShortcutHunter extends ShortcutHunter {

    protected Deque<Integer> moveIndexes;
    protected int moveIndex;

    public BruteForceShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree pathTree, boolean printShortcuts) {
        super(puzzleInfo, maxDepth, pathTree, printShortcuts);
        moveIndexes = new ArrayDeque<>();
        moveIndex = 0;
    }

    @Override
    public List<Shortcut> performSearch() {
        return IntStream.range(0, allowedMoves.length).boxed().parallel()
                .flatMap(index -> this.copy().searchWithMove(index))
                .toList();
    }

    @Override
    protected List<Move> getMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves[i])
                .collect(Collectors.toList());
    }

    @Override
    protected BruteForceShortcutHunter copy() {
        return new BruteForceShortcutHunter(puzzleInfo, maxDepth, pathTree, printShortcuts);
    }

    public Stream<Shortcut> searchWithMove(int index) {
        this.transform(allowedMoves[index]);
        moveIndex = 0;
        moveIndexes.push(index);
        checkForShortcuts();
        while (performStep());
        this.transform(allowedMoves[moveIndexes.pop()].getInverse());
        return this.foundShortcuts.stream();
    }

    protected boolean performStep() {
        while (moveIndex == allowedMoves.length) {
            if (moveIndexes.size() <= 1) {
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

