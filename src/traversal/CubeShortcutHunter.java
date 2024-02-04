//package traversal;
//
//import abstraction.RelativeCubeMove;
//import abstraction.AbstractCubeCycle;
//import abstraction.Cycle;
//import paths.PathRadixTree;
//import model.*;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.*;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//public class CubeShortcutHunter extends ShortcutHunter {
//    private int secondToLastMove;
//
//    private final String cycleFileName;
//
//    public CubeShortcutHunter(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth, Map<Long, PathRadixTree> pathMap) {
//        super(puzzles, puzzleInfo, maxDepth, pathMap);
//        this.cycleFileName = "/Users/johntermaat/Downloads/" + puzzleInfo.getPuzzleType().replaceAll("/", "-") + "-cycles.csv";
//        secondToLastMove = -1;
//    }
//
//    @Override
//    public void performSearch() {
//        System.out.println("Starting shortcut search.");
//        List<ShortcutHunter> allHunters = IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
//                .map(index -> new CubeShortcutHunter(puzzles, puzzleInfo, maxDepth, pathMap).searchWithMove(index))
//                .toList();
//        this.foundShortcuts = allHunters.stream()
//                .flatMap(h -> h.foundShortcuts.stream())
//                .toList();
//        System.out.println("Found " + foundShortcuts.size() + " total shortcuts.");
////        this.foundShortcuts.forEach(Shortcut::print);
////        this.foundShortcuts.forEach(s -> {
////            s.print();
////            s.validate();
////        });
////        System.out.println("Saving cycles to " + cycleFileName);
////        saveCubeShortcutsToFile();
////        System.out.println("activating shortcuts.");
////        this.foundShortcuts.forEach(Shortcut::activate);
////        System.out.println("shortcuts activated.");
//
//    }
//
//    @Override
//    protected boolean performStep() {
//        while (moveIndex == allowedMoves.length) {
//            if (moveIndexes.size() <= 1) {
//                return false;
//            } else {
//                moveIndex = moveIndexes.pop();
//                this.transform(allowedMoves[moveIndex].getInverse());
//                ++moveIndex;
//            }
//        }
//        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse())
//                || moveIndex == moveIndexes.peek()
//                && (allowedMoves[moveIndex].isInversion()
//                || secondToLastMove == moveIndex))) {
//            this.transform(allowedMoves[moveIndex]);
//            secondToLastMove = moveIndexes.peek();
//            moveIndexes.push(moveIndex);
//            moveIndex = 0;
//            checkForShortcuts();
//            if (moveIndexes.size() >= maxDepth) {
//                moveIndex = moveIndexes.pop();
//                this.transform(allowedMoves[moveIndex].getInverse());
//                ++moveIndex;
//                int lastIndex = moveIndexes.pop();
//                secondToLastMove = moveIndexes.peek();
//                moveIndexes.push(lastIndex);
//            }
//        } else {
//            ++moveIndex;
//        }
//        return true;
//    }
//
//    public void saveCubeShortcutsToFile() {
//        List<Cycle> cycles = foundShortcuts.stream()
//                .map(Cycle::new)
//                .toList();
//        cycles.forEach(Cycle::cubeSortMoves);
//        cycles = Cycle.condense(cycles);
//        cycles.forEach(Cycle::runValidation);
//        cycles = new HashSet<>(cycles).stream()
//                .sorted()
//                .toList();
//        Move[][][] structuredMoves = RelativeCubeMove.getStructuredMoveList(allowedMoves);
//        Map<AbstractCubeCycle, Cycle> cycleMap = new HashSet<>(cycles).stream()
//                .filter(c -> !c.getMoves().isEmpty())
//                .collect(Collectors.toMap(c -> new AbstractCubeCycle(c, structuredMoves), Function.identity()));
//        List<AbstractCubeCycle> abstractCycles = cycleMap.keySet().stream()
//                .sorted()
//                .toList();
//        List<AbstractCubeCycle> condensedList = new ArrayList<>();
//        for (int i = 0;i<abstractCycles.size();++i) {
//            boolean include = true;
//            for (int j = i+1;j<abstractCycles.size();++j) {
//                if (abstractCycles.get(j).equals(abstractCycles.get(i))) {
//                    include = false;
//                    continue;
//                }
//            }
//            if (include) {
//                condensedList.add(abstractCycles.get(i));
//            }
//        }
////        AbstractCycle lastCycle = null;
////        for (AbstractCycle cycle : abstractCycles) {
////            if (!cycle.equals(lastCycle)) {
////                condensedList.add(cycle);
////                lastCycle = cycle;
////            }
////        }
//        abstractCycles = condensedList;
//        for (AbstractCubeCycle cycle : abstractCycles) {
//            System.out.println("Examining cycle: " + cycle);
//            System.out.println("Example cycle : " + cycleMap.get(cycle));
//            Set<Move> startMoves = cycle.getValidStartMoves();
//            System.out.println("valid start moves for cycle: " + startMoves);
//
//            for (Move move: startMoves) {
//                System.out.println("Starting with " + move + ": " + cycle.getRelativeTo(move));
//            }
//            if (cycle.isValidForFittingMoves()) {
//                System.out.println("Valid cycle for those fitting moves.");
//            } else {
//                System.out.println("Invalid cycle!!");
//            }
//        }
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(cycleFileName));
//            abstractCycles.forEach(c -> {
//                try {
//                    writer.write(c.toString());
//                    writer.write(cycleMap.get(c).toString());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//            writer.close();
//        } catch (IOException ie) {
//            System.out.println("Error writing cycle file " + cycleFileName);
//            ie.printStackTrace();
//        }
//    }
//}
