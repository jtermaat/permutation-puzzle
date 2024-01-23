package main;

import model.Move;
import model.Puzzle;
import model.PuzzleInfo;
import traversal.CubePermutationChecker;
import traversal.PermutationChecker;
import traversal.SolutionChaser;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PuzzleSolver {
    final static String PUZZLE_TYPE = "cube_2/2/2";
    final static int MAX_DEPTH = 10;
//    public static void main(String[] args) {
//        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
//        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
//        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
//        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
//        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
//                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
//        List<Puzzle> puzzlesToUse = puzzles.stream()
//                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
//                .collect(Collectors.toList());
//        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
//        CubePermutationChecker explorer = new CubePermutationChecker(puzzlesToUse, puzzleInfoToUse, MAX_DEPTH);
//        System.out.println("Starting exploration.");
//        explorer.performSearch();
//        System.out.println("Searching for solutions using " + explorer.getSequencesToSave().size() + " bookmarks.");
//        SolutionChaser chaser = new SolutionChaser(explorer);
//        chaser.performSearch();
//        chaser.writeSolutionsToFile(solutionFilename);
//    }

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

    public static void main(String[] args) {
        int puzzleNum = 17;
        List<String> moveStrings = Arrays.asList("r0","-r1","f0","-d0","d1","-f0","-r0","-f1","d1","r0");

        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE.replaceAll("/", "-") + ".csv";
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
        Puzzle puzzle = puzzles.get(puzzleNum);
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        List<Puzzle> puzzlesToUse = puzzles.stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
                .collect(Collectors.toList());
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(puzzlesToUse.get(0).getPuzzleType());

        PermutationChecker checker = new PermutationChecker(Collections.singletonList(puzzle), puzzleInfoToUse, 1);
        Map<String, Move> moveNameMap = checker.getAllowedMoves().stream()
                .collect(Collectors.toMap(Move::getName, Function.identity()));
        List<Move> moves = moveStrings.stream()
                .map(moveNameMap::get)
                .toList();
        validateSolution(puzzle, puzzleInfoToUse, moves);
    }
}
