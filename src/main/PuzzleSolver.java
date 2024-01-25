package main;

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
//    final static String PUZZLE_TYPE = "globe_1/8";
    final static String PUZZLE_TYPE = "cube_3/3/3";
    final static int MAX_DEPTH = 7;

    public static void main(String[] args) {
        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
        String existingSolutionFilename = "/Users/johntermaat/Downloads/submission-existing.csv";

        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename, existingSolutionFilename, puzzleInfoToUse);
        Map<Long, Map<Permutation, List<Path>>> pathMap = new HashMap<>();
        for (int i = 0;i<puzzles.size();++i) {
            PathCollector collector = new PathCollector(puzzles.get(i), puzzleInfoToUse, MAX_DEPTH, pathMap);
            collector.collectPaths();
        }
        CubeShortcutHunter hunter = new CubeShortcutHunter(puzzles, puzzleInfoToUse, MAX_DEPTH, pathMap);
        int oldTotalMoveLength = puzzles.stream()
                .map(Puzzle::getSolutionLength)
                .reduce(0, Integer::sum);
        System.out.println("Old total move length " + oldTotalMoveLength);
        hunter.performSearch();
        puzzles.forEach(p -> p.getSolution().optimizeRoute());
        int newTotalMoveLength = puzzles.stream()
                .map(Puzzle::getSolutionLength)
                .reduce(0, Integer::sum);
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
            writer.flush();
        } catch (IOException ie) {
            System.out.println("Error writing results to file " + solutionFilename);
            ie.printStackTrace();
        }
    }

    public static void validateSolution(Puzzle puzzle, PuzzleInfo puzzleInfo, List<Move> moves) {
        System.out.println("Validating solution...");
        PermutationChecker checker = new PermutationChecker(Arrays.asList(puzzle), puzzleInfo, 1);
        for (Move move : moves) {
            checker.transform(move);
        }
        boolean matches = true;
        for (int i = 0;i<checker.getPositions().length;++i) {
            if (!checker.getTargetAllowedPositions()[0][i][checker.getPositions()[i]]) {
                matches = false;
            }
        }
        if (matches) {
            System.out.println("Solution works!");
        } else {
            System.out.println("Solution FAILED");
        }
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
