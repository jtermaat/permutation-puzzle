package traversal;

import lombok.Getter;
import lombok.Setter;
import paths.Path;
import model.*;
import paths.PathRadixTree;
import paths.Shortcut;

import java.util.*;

public abstract class ShortcutHunter extends Mutator {

    @Getter
    @Setter
    protected PathRadixTree pathTree;

    @Getter
    protected List<Shortcut> foundShortcuts;
    protected int maxDepth;
    protected PuzzleInfo puzzleInfo;
    protected Move[] allowedMoves;

    @Getter
    @Setter
    protected boolean printShortcuts;

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree pathTree) {
        this(puzzleInfo, maxDepth, pathTree, false);
    }

    public ShortcutHunter(PuzzleInfo puzzleInfo, int maxDepth, PathRadixTree pathTree, boolean printShortcuts) {
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

    protected abstract ShortcutHunter copy();


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

