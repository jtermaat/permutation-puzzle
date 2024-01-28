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
//        this.foundShortcuts.forEach(Shortcut::print);
//        this.foundShortcuts.forEach(Shortcut::validate);
        this.foundShortcuts.forEach(s -> {
            s.print();
            s.validate();
        });
        System.out.println("activating shortcuts.");
        this.foundShortcuts.forEach(Shortcut::activate);
        System.out.println("shortcuts activated.");
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
            moveIndexes.push(moveIndex);
            moveIndex = 0;
            handleSaving();
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

    protected void handleSaving() {
        Map<Permutation, List<Path>> matches = pathMap.get(gameHash);
        Path thisPath = new Path(positions, gameHash);
        if (matches != null) {
            for (Permutation permutation : matches.keySet()) {
                boolean match = true;
                for (int i = 0;i<permutation.getPositions().length;++i) {
                    if (permutation.getPositions()[i] != this.positions[i]) {
                        match = false;
                        break;
                    }
                }
                if (match) {
//                    System.out.println("We have a match.");
                    for (Path path : matches.get(permutation)) {
                        boolean anotherMatch = true;
                        for (int i = 0;i<path.getPositions().length;++i) {
                            if (path.getPositions()[i] != this.positions[i]) {
                                anotherMatch = false;
                                break;
                            }
                        }
                        if (anotherMatch) {
                            System.out.println("Legit match found.");
                            Shortcut newShortcut = new Shortcut(path.getStart(), path.getEnd(), getMoveList());
                            System.out.println("Printing shortcut.");
                            newShortcut.print();
                            System.out.println("Validating shortcut.");
                            newShortcut.validate();
                            foundShortcuts.add(new Shortcut(path.getStart(), path.getEnd(), getMoveList()));
                        } else {
                            System.out.println("False match found.");
                        }
                    }
                }
            }
//            if (matches.containsKey(thisPath)) {
//                for (Path path : matches.get(thisPath)) {
//                    foundShortcuts.add(new Shortcut(path.getStart(), path.getEnd(), getMoveList()));
//                }
//            }
        }
    }

    public List<Move> getMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves[i])
                .collect(Collectors.toList());
    }

}
