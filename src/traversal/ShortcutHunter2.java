package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.Path;
import model.*;
import paths.PathRadixTree2;
import paths.Shortcut;

import java.util.*;

public abstract class ShortcutHunter2 extends Mutator2 {

    @Getter
    @Setter
    protected PathRadixTree2 pathTree;

    @Getter
    protected List<Shortcut> foundShortcuts;
    protected int maxDepth;
    protected PuzzleInfo puzzleInfo;
    protected Move[] allowedMoves;

    @Getter
    @Setter
    protected boolean printShortcuts;

    public ShortcutHunter2(PuzzleInfo puzzleInfo, int maxDepth) {
        this(puzzleInfo, maxDepth, PathCollector2.DEFAULT_PATH_TREE, false);
    }

    public ShortcutHunter2(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree2 pathTree) {
        this(puzzleInfo, maxDepth, pathTree, false);
    }

    public ShortcutHunter2(PuzzleInfo puzzleInfo, int maxDepth, boolean printShortcuts) {
        this(puzzleInfo, maxDepth, PathCollector2.DEFAULT_PATH_TREE, printShortcuts);
    }

    public ShortcutHunter2(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree2 pathTree, boolean printShortcuts) {
        super(puzzleInfo.getAllowedMoves()[0].getNewPositions().length);
        this.pathTree = pathTree;
        this.puzzleInfo = puzzleInfo;
        this.maxDepth = maxDepth;
        this.printShortcuts = printShortcuts;
        allowedMoves = puzzleInfo.getAllowedMoves();
        foundShortcuts = new ArrayList<>();
    }

    public abstract List<Shortcut> performSearch();

    protected abstract List<Move> getMoveList();

    protected abstract ShortcutHunter2 copy();


    protected void checkForShortcuts() {
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

