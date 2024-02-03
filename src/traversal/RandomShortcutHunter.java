package traversal;

import model.PuzzleInfo;
import paths.PathRadixTree;
import paths.Shortcut;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class RandomShortcutHunter extends ShortcutHunter {
    private final long timeLimit;

    private final int threadCount;

    private final static int DEFAULT_THREAD_COUNT = 12;


    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, boolean printShortcuts, long timeLimit, int threadCount) {
        super(puzzleInfo, maxDepth, pathMap, printShortcuts);
        this.timeLimit = timeLimit;
        this.threadCount = threadCount;
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, boolean printShortcuts, long timeLimit, int threadCount) {
        super(puzzleInfo, maxDepth, printShortcuts);
        this.timeLimit = timeLimit;
        this.threadCount = threadCount;
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, long timeLimit, int threadCount) {
        this(puzzleInfo, maxDepth, pathMap, false, timeLimit, threadCount);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, boolean printShortcuts, long timeLimit) {
        this(puzzleInfo, maxDepth, pathMap, printShortcuts, timeLimit, DEFAULT_THREAD_COUNT);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, long timeLimit, int threadCount) {
        this(puzzleInfo, maxDepth, false, timeLimit, threadCount);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth,  boolean printShortcuts, long timeLimit) {
        this(puzzleInfo, maxDepth, printShortcuts, timeLimit, DEFAULT_THREAD_COUNT);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, long timeLimit) {
        this(puzzleInfo, maxDepth, pathMap, false, timeLimit);
    }

    public RandomShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, long timeLimit) {
        this(puzzleInfo, maxDepth, timeLimit, DEFAULT_THREAD_COUNT);
    }

    @Override
    public List<Shortcut> performSearch() {
        System.out.println("Starting random shortcut search.");
        long endTime = System.currentTimeMillis() + timeLimit;
        return  IntStream.range(0, threadCount).boxed().parallel()
                .map(i -> new RandomShortcutHunter(puzzleInfo, maxDepth, pathMap, printShortcuts, timeLimit, threadCount))
                .flatMap(h -> h.searchUntil(endTime))
                .toList();
    }

    private Stream<Shortcut> searchUntil(long time) {
        while (System.currentTimeMillis() < time) {
            resetPositions();
            randomSearch();
        }
        return this.foundShortcuts.stream();
    }

    private void randomSearch() {

    }
}
