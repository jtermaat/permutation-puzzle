package traversal;

import model.Path;
import model.Puzzle;
import model.PuzzleInfo;
import model.Shortcut;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class CubeShortcutHunter extends ShortcutHunter {
    private int secondToLastMove;

    public CubeShortcutHunter(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth, Map<Long, Map<Permutation, List<Path>>> pathMap) {
        super(puzzles, puzzleInfo, maxDepth, pathMap);
        secondToLastMove = -1;
    }

    @Override
    public void performSearch() {
        System.out.println("Starting shortcut search.");
        List<ShortcutHunter> allHunters = IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
                .map(index -> new CubeShortcutHunter(puzzles, puzzleInfo, maxDepth, pathMap).searchWithMove(index))
                .toList();
        this.foundShortcuts = allHunters.stream()
                .flatMap(h -> h.foundShortcuts.stream())
                .toList();
        System.out.println("Found " + foundShortcuts.size() + " total shortcuts.");
        this.foundShortcuts.forEach(Shortcut::activate);
    }

    @Override
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
        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse())
                || moveIndex == moveIndexes.peek()
                && (allowedMoves[moveIndex].isInversion()
                || secondToLastMove == moveIndex))) {
            this.transform(allowedMoves[moveIndex]);
            handleSaving();
            if (moveIndexes.size() < maxDepth) {
                secondToLastMove = moveIndexes.peek();
                moveIndexes.push(moveIndex);
                moveIndex = 0;
            } else {
                this.transform(allowedMoves[moveIndex].getInverse());
                int lastIndex = moveIndexes.pop();
                secondToLastMove = moveIndexes.peek();
                moveIndexes.push(lastIndex);
                ++moveIndex;
            }
        } else {
            ++moveIndex;
        }
        return true;
    }
}
