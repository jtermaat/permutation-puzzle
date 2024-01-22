package main;

import model.Puzzle;
import model.PuzzleInfo;
import traversal.PermutationChecker;
import traversal.SolutionChaser;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PuzzleSolver {
    final static String PUZZLE_TYPE = "cube_2/2/2";
    public static void main(String[] args) {
        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        String solutionFilename = "/Users/johntermaat/Downloads/submission-" + PUZZLE_TYPE + ".csv";
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        int testPuzzleId = 20;
        List<Puzzle> puzzlesToUse = puzzles.stream()
                .filter(p -> p.getPuzzleType().equals(PUZZLE_TYPE))
                .collect(Collectors.toList());
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(PUZZLE_TYPE);
        PermutationChecker explorer = new PermutationChecker(puzzlesToUse, puzzleInfoToUse, 9);
        System.out.println("Starting exploration.");
        explorer.performSearch();
        System.out.println("Searching for solutions using " + explorer.getSequencesToSave().size() + " bookmarks.");
        SolutionChaser chaser = new SolutionChaser(explorer);
        chaser.performSearch();
        chaser.writeSolutionsToFile(solutionFilename);
    }
}
