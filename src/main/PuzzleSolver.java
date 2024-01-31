package main;

import paths.PathRadixTree;
import model.*;
import traversal.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PuzzleSolver {
//    final static String PUZZLE_TYPE = "cube_2/2/2";
//    final static String PUZZLE_TYPE = "cube_3/3/3";
final static String PUZZLE_TYPE = "cube_2/2/2";
//    final static String PUZZLE_TYPE = "globe_3/33";
//    final static String PUZZLE_TYPE = "wreath_21/21";
//    final static String PUZZLE_TYPE = "cube_10/10/10";
    final static int MAX_DEPTH = 3;

    final static boolean IS_CUBE = true;

    public static void main(String[] args) {
        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
//        String existingSolutionFilename = "/Users/johntermaat/Downloads/submission-existing.csv";
//        String existingSolutionFilename = "/Users/johntermaat/Downloads/submission.csv";
        String existingSolutionFilename = "/Users/johntermaat/Downloads/sample_submission.csv";

        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        System.out.println("useing puzzle info " + puzzleInfoToUse.getPuzzleType());
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename, existingSolutionFilename, puzzleInfoToUse).stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
//                .limit(1)
                .toList();
        Map<Long, PathRadixTree> pathMap = new HashMap<>();
        Long time = System.currentTimeMillis();
        System.out.println("Collecting paths.");
        for (int i = 0;i<puzzles.size();++i) {
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
                .map(p -> p.getSolution().toList(null).size())
                .reduce(0, Integer::sum);
//        int oldTotalMoveLength = puzzles.stream()
//                .map(Puzzle::getSolutionLength)
//                .reduce(0, Integer::sum);
        System.out.println("Old total move length " + oldTotalMoveLength);
        System.out.println("Starting search.");
        hunter.performSearch();
        System.out.println("Completed search at " + (System.currentTimeMillis() - time) / 1000+ " seconds.");
        System.out.println("Optimizing route.");
        puzzles.forEach(p -> p.getSolution().optimizeRoute());
        System.out.println("Completed optimizing routes at " + (System.currentTimeMillis() - time) / 1000+ " seconds.");
        int newTotalMoveLength = puzzles.stream()
                .map(p -> p.getSolution().toList(null).size())
                .reduce(0, Integer::sum);
//        validateSolutions(puzzles);
        System.out.println("Old total move length " + oldTotalMoveLength);
        System.out.println("New total move length " + newTotalMoveLength);
        writeSolutionsToFile(puzzles, solutionFilename);

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

    public static void writeSolutionsToFile(List<Puzzle> puzzles, String solutionFilename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(solutionFilename));
            for (int i = 0;i<puzzles.size();++i) {
                List<Move> allMoves = puzzles.get(i).getSolution().tolist();
                if (!allMoves.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(puzzles.get(i).getId()).append(",");
                    sb.append(allMoves.get(0).getName());
                    for (int j = 1; j < allMoves.size(); ++j) {
                        sb.append(".").append(allMoves.get(j).getName());
                    }
                    sb.append("\n");
                    System.out.println("For target " + i + ", writing: " + sb.toString());
                    writer.write(sb.toString());
                }
            }
            writer.close();
        } catch (IOException ie) {
            System.out.println("Error writing results to file " + solutionFilename);
            ie.printStackTrace();
        }
    }

    public static void validateSolutions(List<Puzzle> puzzles) {
        for (Puzzle puzzle : puzzles) {
            List<Move> moves = puzzle.getSolution().tolist();
            validateSolution(puzzle, moves);
        }
    }

    public static void validateSolution(Puzzle puzzle, List<Move> moves) {
        System.out.println("Validating solution for puzzle " + puzzle.getId());
        Permutation checker = new Permutation(puzzle.getInitialState());
        for (Move move : moves) {
            checker.transform(move);
        }
        boolean matches = true;
        boolean[][] allowedPositionsSolution = puzzle.getSolutionState();
        for (int i = 0;i<checker.getPositions().length;++i) {
            if (!allowedPositionsSolution[i][checker.getPositions()[i]]) {
                matches = false;
            }
        }
        if (matches) {
            System.out.println("Solution " + puzzle.getId() + " works!");
        } else {
            System.out.println("Solution " + puzzle.getId() + " FAILED");
        }
    }

    public static boolean validateEquality(List<Move> list1, List<Move> list2) {
//        System.out.println("Validating moveList equality.");
        if (list1.isEmpty() && list2.isEmpty()) {
            return true;
        }
        int length = list1.isEmpty() ? list2.getFirst().getNewPositions().length : list1.getFirst().getNewPositions().length;
        short[] startPositions1 = new short[length];
        short[] startPositions2 = new short[length];
        for (short i = 0;i<startPositions1.length;++i) {
            startPositions1[i] = i;
            startPositions2[i] = i;
        }

        final Permutation checker1 = new Permutation(startPositions1);
        Permutation checker2 = new Permutation(startPositions2);
        list1.forEach(checker1::transform);
        list2.forEach(checker2::transform);

        boolean matches = true;
        for (int i = 0;i<checker1.getPositions().length;++i) {
            if (checker1.getPositions()[i] != checker2.getPositions()[i]) {
                matches = false;
            }
        }
        if (matches) {
//            System.out.println("These move lists match!!!");
        } else {
//            System.out.println("These move lists don't match.");
        }
        return matches;
    }

//    public static void main(String[] args) {
//        int puzzleNum = 17;
//        List<String> moveStrings = Arrays.asList("r0","-r1","f0","-d0","d1","-f0","-r0","-f1","d1","r0");
//
//        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
//        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
//        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
//        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
//        Puzzle puzzle = puzzles.get(puzzleNum);
//        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
//                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
//        List<Puzzle> puzzlesToUse = puzzles.stream()
//                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
//                .collect(Collectors.toList());
//        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(puzzlesToUse.get(0).getPuzzleType());
//
//        PermutationChecker checker = new PermutationChecker(Collections.singletonList(puzzle), puzzleInfoToUse, 1);
//        Map<String, Move> moveNameMap = checker.getAllowedMoves().stream()
//                .collect(Collectors.toMap(Move::getName, Function.identity()));
//        List<Move> moves = moveStrings.stream()
//                .map(moveNameMap::get)
//                .toList();
//        validateSolution(puzzle, puzzleInfoToUse, moves);
//    }
}
