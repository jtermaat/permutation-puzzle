package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.Path;
import paths.PathRadixTree;
import model.*;
import paths.Shortcut;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ShortcutHunter extends Mutator {
    protected Map<Long, PathRadixTree> pathMap;

    @Getter
    protected List<Shortcut> foundShortcuts;
    protected int maxDepth;
    protected PuzzleInfo puzzleInfo;
    protected Deque<Integer> moveIndexes;
    protected int moveIndex;
    protected Move[] allowedMoves;

    @Getter
    @Setter
    protected boolean printShortcuts;

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth) {
        this(puzzleInfo, maxDepth, PathCollector.DEFAULT_PATH_MAP, false);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap) {
        this(puzzleInfo, maxDepth, pathMap, false);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, boolean printShortcuts) {
        this(puzzleInfo, maxDepth, PathCollector.DEFAULT_PATH_MAP, printShortcuts);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap, boolean printShortcuts) {
        super();
        this.pathMap = pathMap;
        this.puzzleInfo = puzzleInfo;
        this.maxDepth = maxDepth;
        this.printShortcuts = printShortcuts;
        moveIndexes = new ArrayDeque<>();
        moveIndex = 0;
        allowedMoves = puzzleInfo.getAllowedMoves();
        foundShortcuts = new ArrayList<>();
    }

    protected abstract List<Shortcut> performSearch();

    public void searchAndActivateShortcuts() {
        foundShortcuts = this.performSearch();
        System.out.println("Activating " + foundShortcuts.size() + " total shortcuts.");
        this.performSearch().forEach(Shortcut::activate);
        System.out.println("Shortcuts activated.");
    }

    protected void checkForShortcuts() {
        PathRadixTree pathTree = pathMap.get(gameHash);
        if (pathTree != null) {
            List<Path> paths = pathTree.get(positions);
            if (paths != null) {
                for (Path path : paths) {
                    Shortcut shortcut = new Shortcut(path.getStart(), path.getEnd(), getMoveList());
                    if (printShortcuts) {
                        shortcut.print();
                    }
                    foundShortcuts.add(shortcut);
                }
            }
        }
    }

    protected List<Move> getMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves[i])
                .collect(Collectors.toList());
    }
}
