package main;

import model.Move;
import model.Puzzle;
import model.PuzzleInfo;
import model.RelativeCubeMove;
import paths.*;
import traversal.CubeShortcutHunter;
import traversal.PathCollector;
import traversal.ShortcutHunter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NewDriver {

    final static String PUZZLE_TYPE = "cube_8/8/8";
    //    final static String PUZZLE_TYPE = "globe_3/33";
//    final static String PUZZLE_TYPE = "wreath_21/21";
//    final static String PUZZLE_TYPE = "cube_10/10/10";
    final static int MAX_DEPTH = 3;

    final static boolean IS_CUBE = true;

    final static String OUTPUT_FILE = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
    final static String OLD_BEST_SOLUTION = "/Users/johntermaat/Downloads/submission.csv";

    final static String CYCLE_FINDING_SOLUTION = "/Users/johntermaat/Downloads/sample_submission.csv";

    final static String PUZZLES_FILENAME = "/Users/johntermaat/Downloads/puzzles.csv";
    final static String PUZZLE_INFOS_FILENAME = "/Users/johntermaat/Downloads/puzzle_info.csv";

    public static List<AbstractCubeCycle> getCycles() {

    }
    public static void main(String[] args) {
//        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
//        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
//        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
////        String existingSolutionFilename = "/Users/johntermaat/Downloads/submission-existing.csv";
////        String existingSolutionFilename = "/Users/johntermaat/Downloads/submission.csv";
//        String existingSolutionFilename = "/Users/johntermaat/Downloads/sample_submission.csv";

        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        System.out.println("useing puzzle info " + puzzleInfoToUse.getPuzzleType());
        List<Puzzle> puzzlesCycleFinding = Puzzle.readPuzzleList(puzzlesFilename, CYCLE_FINDING_SOLUTION, puzzleInfoToUse).stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
                .toList();
        List<Puzzle> puzzles
        Map<Long, PathRadixTree> pathMap = new HashMap<>();
        Long time = System.currentTimeMillis();
        System.out.println("Collecting paths.");
        for (int i = 0; i < puzzles.size(); ++i) {
            PathCollector collector = new PathCollector(puzzles.get(i), puzzleInfoToUse, MAX_DEPTH, pathMap);
            collector.collectPaths();
        }
        System.out.println("Finished collecting paths at " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
        ShortcutHunter hunter;
        if (IS_CUBE) {
            hunter = new CubeShortcutHunter(puzzles, puzzleInfoToUse, MAX_DEPTH, pathMap);
        } else {
            hunter = new ShortcutHunter(puzzles, puzzleInfoToUse, MAX_DEPTH, pathMap);
        }
        int oldTotalMoveLength = puzzles.stream()
                .map(Puzzle::getSolutionLength)
                .reduce(0, Integer::sum);
        System.out.println("Old total move length " + oldTotalMoveLength);
        System.out.println("Starting search.");
        hunter.performSearch();

        System.out.println("Completed search at " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
        List<Shortcut> firstShortcutList = hunter.getFoundShortcuts();
        System.out.println("Found " + firstShortcutList.size() + " shortcuts on initial search.");
        System.out.println("Converting shortcuts to abstract cycles.");

        List<Cycle> cycles = firstShortcutList.stream()
                .map(Cycle::new)
                .toList();
        cycles.forEach(Cycle::cubeSortMoves);
        cycles = Cycle.condense(cycles);
//        cycles.forEach(Cycle::runValidation);
        cycles = new HashSet<>(cycles).stream()
                .sorted()
                .toList();
        Move[][][] structuredMoves = RelativeCubeMove.getStructuredMoveList(puzzleInfoToUse.getAllowedMoves());
        Map<AbstractCubeCycle, Cycle> cycleMap = new HashSet<>(cycles).stream()
                .filter(c -> !c.getMoves().isEmpty())
                .collect(Collectors.toMap(c -> new AbstractCubeCycle(c, structuredMoves), Function.identity()));
        List<AbstractCubeCycle> abstractCycles = cycleMap.keySet().stream()
                .sorted()
                .toList();
        List<AbstractCubeCycle> condensedList = new ArrayList<>();
        for (int i = 0;i<abstractCycles.size();++i) {
            boolean include = true;
            for (int j = i+1;j<abstractCycles.size();++j) {
                if (abstractCycles.get(j).equals(abstractCycles.get(i))) {
                    include = false;
                    continue;
                }
            }
            if (include) {
                condensedList.add(abstractCycles.get(i));
            }
        }
        System.out.println("Found " + condensedList.size() + " abstract cycles.");

        for (Puzzle puzzle : puzzles) {
            System.out.println("Applying abstract cycles to puzzle " + puzzle.getId());
            AbstractCycleChecker checker = new AbstractCycleChecker(condensedList, puzzle);
            List<Shortcut> shortcuts = checker.setupShortcuts();
            System.out.println("Found " + shortcuts.size() + " shortcuts.");
            System.out.println("Activating shortcuts.");
            shortcuts.forEach(Shortcut::activate);
        }

        PuzzleSolver.writeSolutionsToFile(SOLUTION_FILENAME);
//        AbstractCycle lastCycle = null;
//        for (AbstractCycle cycle : abstractCycles) {
//            if (!cycle.equals(lastCycle)) {
//                condensedList.add(cycle);
//                lastCycle = cycle;
//            }
//        }
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
//



        //        System.out.println("Optimizing route.");
//        puzzles.forEach(p -> p.getSolution().optimizeRoute());
//        System.out.println("Completed optimizing routes at " + (System.currentTimeMillis() - time) / 1000 + " seconds.");
//        int newTotalMoveLength = puzzles.stream()
//                .map(p -> p.getSolution().toList(null).size())
//                .reduce(0, Integer::sum);
////        validateSolutions(puzzles);
//        System.out.println("Old total move length " + oldTotalMoveLength);
//        System.out.println("New total move length " + newTotalMoveLength);
//        writeSolutionsToFile(puzzles, solutionFilename);

//        PermutationChecker explorer = new CubePermutationChecker(puzzlesToUse, puzzleInfoToUse, MAX_DEPTH);
//        System.out.println("Starting exploration.");
//        Long time = System.currentTimeMillis();
//        explorer.performSearch();
//        System.out.println("Searched for " + ((System.currentTimeMillis() - time) / 60000) + " minutes.");
//        System.out.println("Searching for solutions using " + explorer.getSequencesToSave().size() + " bookmarks.");
//        SolutionChaser chaser = new SolutionChaser(explorer, SOLUTION_MAX_DEPTH);
//        chaser.performSearch();
//        chaser.writeSolutionsToFile(solutionFilename);
    }
}

