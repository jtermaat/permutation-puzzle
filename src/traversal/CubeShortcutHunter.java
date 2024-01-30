package traversal;

import paths.AbstractCycle;
import paths.Cycle;
import paths.PathRadixTree;
import model.*;
import paths.Shortcut;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CubeShortcutHunter extends ShortcutHunter {
    private int secondToLastMove;

    private final String cycleFileName;

    public CubeShortcutHunter(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap) {
        super(puzzles, puzzleInfo, maxDepth, pathMap);
        this.cycleFileName = "/Users/johntermaat/Downloads/" + puzzleInfo.getPuzzleType().replaceAll("/", "-") + "-cycles.csv";
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
//        this.foundShortcuts.forEach(Shortcut::print);
//        this.foundShortcuts.forEach(s -> {
//            s.print();
//            s.validate();
//        });
        System.out.println("Saving cycles to " + cycleFileName);
        saveCubeShortcutsToFile();
        System.out.println("activating shortcuts.");
        this.foundShortcuts.forEach(Shortcut::activate);
        System.out.println("shortcuts activated.");
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
            secondToLastMove = moveIndexes.peek();
            moveIndexes.push(moveIndex);
            moveIndex = 0;
            handleSaving();
            if (moveIndexes.size() >= maxDepth) {
                moveIndex = moveIndexes.pop();
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
                int lastIndex = moveIndexes.pop();
                secondToLastMove = moveIndexes.peek();
                moveIndexes.push(lastIndex);
            }
        } else {
            ++moveIndex;
        }
        return true;
    }

    public void saveCubeShortcutsToFile() {
        List<Cycle> cycles = foundShortcuts.stream()
                .map(Cycle::new)
                .toList();
        cycles.forEach(Cycle::cubeSortMoves);
        cycles = Cycle.condense(cycles);
        cycles.forEach(Cycle::runValidation);
        cycles = new HashSet<>(cycles).stream()
                .sorted()
                .toList();
        Move[][][] structuredMoves = RelativeMove.getStructuredMoveList(allowedMoves);
        Map<AbstractCycle, Cycle> cycleMap = new HashSet<>(cycles).stream()
                .filter(c -> !c.getMoves().isEmpty())
                .collect(Collectors.toMap(c -> new AbstractCycle(c, structuredMoves), Function.identity()));
        List<AbstractCycle> abstractCycles = cycleMap.keySet().stream()
                .sorted()
                .toList();
        List<AbstractCycle> condensedList = new ArrayList<>();
        AbstractCycle lastCycle = null;
        for (AbstractCycle cycle : abstractCycles) {
            if (!cycle.equals(lastCycle)) {
                condensedList.add(cycle);
                lastCycle = cycle;
            }
        }
        abstractCycles = condensedList;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(cycleFileName));
            abstractCycles.forEach(c -> {
                try {
                    writer.write(c.toString());
                    writer.write(cycleMap.get(c).toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.close();
        } catch (IOException ie) {
            System.out.println("Error writing cycle file " + cycleFileName);
            ie.printStackTrace();
        }
    }
}
