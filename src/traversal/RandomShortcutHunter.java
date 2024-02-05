package traversal;

import lombok.Setter;
import model.Move;
import model.PuzzleInfo;
import paths.PathRadixTree;
import paths.Shortcut;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomShortcutHunter extends ShortcutHunter {
    private final long timeLimit;

    @Setter
    private int maxThreadCount;

    private final static int DEFAULT_THREAD_COUNT = 12;

    private final Random random;

    @Setter
    private int minDepth;

    private List<Move> moveList;


    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree pathTree, boolean printShortcuts, long timeLimit, int maxThreadCount, int minDepth) {
        super(puzzleInfo, maxDepth, pathTree, printShortcuts);
        this.timeLimit = timeLimit;
        this.maxThreadCount = maxThreadCount;
        this.random = new Random();
        this.minDepth = minDepth;
        moveList = new ArrayList<>(maxDepth);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree pathTree, int minutes) {
        this(puzzleInfo, maxDepth, pathTree, false, (long)minutes*60L*1000L, DEFAULT_THREAD_COUNT, 0);
    }

    @Override
    public List<Shortcut> performSearch() {
        long endTime = System.currentTimeMillis() + timeLimit;
        return  IntStream.range(0, maxThreadCount).boxed().parallel()
                .map(i -> this.copy())
                .flatMap(h -> h.searchUntil(endTime))
                .toList();
    }

    @Override
    public List<Move> getMoveList() {
        return new ArrayList<>(this.moveList);
    }

    @Override
    protected int getLength() {
        return moveList.size();
    }

    protected RandomShortcutHunter copy() {
        return new RandomShortcutHunter(puzzleInfo, maxDepth, pathTree, printShortcuts, timeLimit, maxThreadCount, minDepth);
    }

    @Override
    public void resetPositions() {
        super.resetPositions();
        this.moveList = new ArrayList<>(this.maxDepth);
    }

    private Stream<Shortcut> searchUntil(long time) {
        while (System.currentTimeMillis() < time) {
            resetPositions();
            randomSearch();
        }
        return this.foundShortcuts.stream();
    }

    private void randomSearch() {
        int count = 0;
        while (count < this.maxDepth) {
            Move nextMove = allowedMoves[random.nextInt(allowedMoves.length)];
            moveList.add(nextMove);
            this.transform(nextMove);
            if (count >= this.minDepth) {
                checkForShortcuts();
            }
            ++count;
        }
    }
}
