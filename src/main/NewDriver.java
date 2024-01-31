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

    final static String PUZZLE_TYPE = "cube_4/4/4";
//    final static String PUZZLE_TYPE = "cube_8/8/8";
    //    final static String PUZZLE_TYPE = "globe_3/33";
//    final static String PUZZLE_TYPE = "wreath_21/21";
//    final static String PUZZLE_TYPE = "cube_10/10/10";
    final static int MAX_DEPTH = 5;

    final static boolean IS_CUBE = true;

    final static String OUTPUT_FILE = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
//    final static String OLD_BEST_SOLUTION = "/Users/johntermaat/Downloads/submission.csv";

    final static String OLD_BEST_SOLUTION = "/Users/johntermaat/Downloads/sample_submission.csv";

    final static String CYCLE_FINDING_SOLUTION = "/Users/johntermaat/Downloads/sample_submission.csv";

    final static String PUZZLES_FILENAME = "/Users/johntermaat/Downloads/puzzles.csv";
    final static String PUZZLE_INFOS_FILENAME = "/Users/johntermaat/Downloads/puzzle_info.csv";

    public static List<AbstractCubeCycle> getCycles() {
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(PUZZLE_INFOS_FILENAME).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        System.out.println("useing puzzle info " + puzzleInfoToUse.getPuzzleType());
        List<Puzzle> puzzles = Puzzle.readPuzzleList(PUZZLES_FILENAME, CYCLE_FINDING_SOLUTION, puzzleInfoToUse).stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
                .toList();
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
//        int oldTotalMoveLength = puzzles.stream()
//                .map(Puzzle::getSolutionLength)
//                .reduce(0, Integer::sum);
//        int newTotalMoveLength = puzzles.stream()
//                .map(p -> p.getSolution().toList(null).size())
//                .reduce(0, Integer::sum);
//        System.out.println("Old total move length " + oldTotalMoveLength);
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
        return condensedList;
    }
    public static void main(String[] args) {
        List<AbstractCubeCycle> cycles = getCycles();
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(PUZZLE_INFOS_FILENAME).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        System.out.println("useing puzzle info " + puzzleInfoToUse.getPuzzleType());
        List<Puzzle> puzzles = Puzzle.readPuzzleList(PUZZLES_FILENAME, OLD_BEST_SOLUTION, puzzleInfoToUse).stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
                .toList();

        for (Puzzle puzzle : puzzles) {
            System.out.println("Applying abstract cycles to puzzle " + puzzle.getId());
            AbstractCycleChecker checker = new AbstractCycleChecker(cycles, puzzle);
            List<Shortcut> shortcuts = checker.setupShortcuts();
            for (Shortcut shortcut : shortcuts) {
                System.out.print("Shortcut: ");
                shortcut.print();
                Cycle cycle = new Cycle(shortcut);
                System.out.println("Is it a valid cycle?");
                System.out.println(cycle.isValid());
            }
            System.out.println("Found " + shortcuts.size() + " shortcuts.");
            System.out.println("Activating shortcuts.");
            int oldLength = puzzle.getSolution().toList(null).size();
            shortcuts.forEach(Shortcut::activate);
            puzzle.getSolution().optimizeRoute();
            int newLength = puzzle.getSolution().toList(null).size();
            System.out.println("Old length: " + oldLength);
            System.out.println("New length: "  + newLength);
        }

        PuzzleSolver.writeSolutionsToFile(puzzles, OUTPUT_FILE);
    }
}

