package traversal;

import model.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ShortcutHunter extends Permutation {
    protected Map<Long, Map<Permutation, List<Path>>> pathMap;
    protected List<Shortcut> foundShortcuts;
    protected int maxDepth;
    protected List<Puzzle> puzzles;
    protected PuzzleInfo puzzleInfo;
    protected Deque<Integer> moveIndexes;
    protected int moveIndex;
    protected Move[] allowedMoves;

    public ShortcutHunter(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth, Map<Long, Map<Permutation, List<Path>>> pathMap) {
        super(puzzles.get(0).getInitialState());
        this.pathMap = pathMap;
        this.puzzles = puzzles;
        this.puzzleInfo = puzzleInfo;
        this.maxDepth = maxDepth;
        moveIndexes = new ArrayDeque<>();
        moveIndex = 0;
        allowedMoves = puzzleInfo.getAllowedMoves();
        foundShortcuts = new ArrayList<>();
    }

    public void performSearch() {
        System.out.println("Starting shortcut search.");
        List<ShortcutHunter> allHunters = IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
                .map(index -> new ShortcutHunter(puzzles, puzzleInfo, maxDepth, pathMap).searchWithMove(index))
                .toList();
        this.foundShortcuts = allHunters.stream()
                .flatMap(h -> h.foundShortcuts.stream())
                .toList();
        System.out.println("Found " + foundShortcuts.size() + " total shortcuts.");
        this.foundShortcuts.forEach(Shortcut::activate);
    }

    public ShortcutHunter searchWithMove(int index) {
        if (moveIndexes.isEmpty() || !(allowedMoves[index].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
            this.transform(allowedMoves[index]);
            moveIndex = 0;
            moveIndexes.push(index);
            handleSaving();
            while (performStep());
            this.transform(allowedMoves[moveIndexes.pop()].getInverse());
        }
        return this;
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
            handleSaving();
            if (moveIndexes.size() < maxDepth) {
                moveIndexes.push(moveIndex);
                moveIndex = 0;
            } else {
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
            }
        } else {
            ++moveIndex;
        }
        return true;
    }

    protected void handleSaving() {
        Map<Permutation, List<Path>> matches = pathMap.get(gameHash);
        if (matches != null) {
            if (matches.containsKey(this)) {
                for (Path path : matches.get(this)) {
                    foundShortcuts.add(new Shortcut(path.getStart(), path.getEnd(), getMoveList()));
                }
            }
        }
    }

    public List<Move> getMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves[i])
                .collect(Collectors.toList());
    }

}
