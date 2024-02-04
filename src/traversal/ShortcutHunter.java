package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.Path;
import paths.PathRadixTree;
import model.*;
import paths.PathRadixTree2;
import paths.Shortcut;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ShortcutHunter extends Mutator {
    protected Map<Long, PathRadixTree2> pathMap;

    @Getter
    protected List<Shortcut> foundShortcuts;
    protected int maxDepth;
    protected PuzzleInfo puzzleInfo;
    protected Move[] allowedMoves;

    @Getter
    @Setter
    protected boolean printShortcuts;

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth) {
        this(puzzleInfo, maxDepth, PathCollector.DEFAULT_PATH_MAP, false);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree2> pathMap) {
        this(puzzleInfo, maxDepth, pathMap, false);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, boolean printShortcuts) {
        this(puzzleInfo, maxDepth, PathCollector.DEFAULT_PATH_MAP, printShortcuts);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree2> pathMap, boolean printShortcuts) {
        super(puzzleInfo.getAllowedMoves()[0].getNewPositions().length);
        this.pathMap = pathMap;
        this.puzzleInfo = puzzleInfo;
        this.maxDepth = maxDepth;
        this.printShortcuts = printShortcuts;
        allowedMoves = puzzleInfo.getAllowedMoves();
        foundShortcuts = new ArrayList<>();
    }

    public abstract List<Shortcut> performSearch();

    protected abstract List<Move> getMoveList();

    protected abstract ShortcutHunter copy();

    protected void checkForShortcuts() {
        PathRadixTree2 pathTree = pathMap.get(gameHash);
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
}
