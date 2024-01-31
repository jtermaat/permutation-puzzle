package main;

import model.Move;
import model.Puzzle;
import model.PuzzleInfo;
import model.RelativeCubeMove;
import paths.AbstractCubeCycle;
import paths.Cycle;
import traversal.Permutation;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CycleTester {

//    final static String PUZZLE_TYPE = "cube_3/3/3";
    final static String PUZZLE_TYPE = "cube_2/2/2";

    final static int MAX_DEPTH = 3;
    final static boolean IS_CUBE = true;
    public static String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
    public static String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";

    public static void main(String[] args) {
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

//        String testString = "-r0.-r1.-r2.-f1.r0.r1.r2.-d1";
//        String testString = "-r0.-r1.-r2.-f2.r0.r1.r2.-d0";
//        String testString = "-r0.-r1.-f1.r0.r1.-d0";
//        String testString2 = "f0.f1.-d0.-f0.-f1.r0";
        String testString2 = "r0.-r1.f0.f1.-d1.-d1.-d1.-r0.-r1.-f0.r1.d1.d1.d1.-f0.-f1.-r0.f0";
        List<String> testStrings = Arrays.asList(testString2);

        for (String test : testStrings) {

            Permutation testPermutation = new Permutation();
            Move[][][] moves = RelativeCubeMove.getStructuredMoveList(puzzleInfoToUse.getAllowedMoves());

            Cycle testCycle = new Cycle(test, Arrays.asList(puzzleInfoToUse.getAllowedMoves()));
            System.out.println("Starting with " + testCycle);
            System.out.println("Is it valid? " + testCycle.isValid());
            AbstractCubeCycle abstractCycle = new AbstractCubeCycle(testCycle, moves);
            Set<Move> validStartMoves = abstractCycle.getValidStartMoves();
            System.out.println("Valid start moves: " + validStartMoves);
        }
    }
}

//
//    Validating moveList equality.
//        These move lists don't match.
//        Starting with r1 we get FAILED ,r1,-d2,-d1,-d2,f1,d2,d1,d2
//
//        Validating moveList equality.
//        These move lists don't match.
//        Starting with -r1 we get FAILED ,-r1,d2,d1,d2,-f1,-d2,-d1,-d2
//
//        we got arrayIndex out of bounds trying to get cycle relative to r2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to -r2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to d0 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to -d0 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        Validating moveList equality.
//        These move lists don't match.
//        Starting with d1 we get FAILED ,d1,-f2,-f1,-f2,r1,f2,f1,f2
//
//        Validating moveList equality.
//        These move lists don't match.
//        Starting with -d1 we get FAILED ,-d1,f2,f1,f2,-r1,-f2,-f1,-f2
//
//        we got arrayIndex out of bounds trying to get cycle relative to d2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to -d2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to f0 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to -f0 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        Validating moveList equality.
//        These move lists don't match.
//        Starting with f1 we get FAILED ,f1,-r2,-r1,-r2,d1,r2,r1,r2
//
//        Validating moveList equality.
//        These move lists don't match.
//        Starting with -f1 we get FAILED ,-f1,r2,r1,r2,-d1,-r2,-r1,-r2
//
//        we got arrayIndex out of bounds trying to get cycle relative to f2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)
//
//        we got arrayIndex out of bounds trying to get cycle relative to -f2 for ,(1.-1.0.0),(0.1.0.0),(0.-1.0.0),(1.1.0.0),(2.-1.0.1),(0.1.0.0),(0.-1.0.0),(2.1.0.1)