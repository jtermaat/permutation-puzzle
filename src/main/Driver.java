package main;

import model.Move;
import model.Puzzle;
import model.PuzzleInfo;
import traversal.BackwardPermutationChecker;
import traversal.ForwardPermutationChecker;
import traversal.PermutationChecker;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Driver {

    public static final int START_DEPTH = 15;
    public static void main(String[] args) {
        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(p -> p.getPuzzleType(), Function.identity()));
        int testPuzzleId = 20;
        Puzzle puzzleToUse = puzzles.stream()
                .filter(p -> p.getId() == testPuzzleId)
                .findFirst().get();
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(puzzleToUse.getPuzzleType());
        Map<Long, Integer> forwardCycleMap = new HashMap<>();
        Map<Long, List<Move>> backwardTrailMap = new ConcurrentHashMap<>();
        BackwardPermutationChecker backward = new BackwardPermutationChecker(puzzleToUse.getInitialState(),
                puzzleToUse.getSolutionState(),
                puzzleInfoToUse.getAllowedMoves(),
                START_DEPTH,
                (int)(100.0 / puzzleInfoToUse.getAllowedMoves().size()),
                puzzleToUse.getNumWildcards(),
                backwardTrailMap);
        ForwardPermutationChecker forward = new ForwardPermutationChecker(puzzleToUse.getInitialState(),
                puzzleToUse.getSolutionState(),
                puzzleInfoToUse.getAllowedMoves(),
                START_DEPTH,
                (int)(100.0 / puzzleInfoToUse.getAllowedMoves().size()),
                puzzleToUse.getNumWildcards(),
                forwardCycleMap,
                backwardTrailMap);

        runTest(forward, backward, puzzleInfoToUse);
        Thread forwardThread = new Thread(forward);
        Thread backwardThread = new Thread(backward);
        Long startTime = System.currentTimeMillis();
//        backwardThread.start();
//        System.out.println("Letting the backward thread get a headstart.");


//        while (backwardThread.isAlive()) {
            Map<Long, List<Move>> recentMap = backward.getMostRecentFoundHashes();
//            if (recentMap != null) {
//                forward.setTargetFoundHashes(backward.getMostRecentFoundHashes());
//            }
        forwardThread.start();
        while (forwardThread.isAlive()) {
            try {
                Thread.sleep(20000L);
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }
            System.out.println();
            Long duration = System.currentTimeMillis() - startTime;
            System.out.println("Now searching for " + duration / 1000.0 + " seconds");
            System.out.println("Forward explored: " + forward.getCount());
//            System.out.println("Backward explored: " + backward.getCount());
            System.out.println("Cycles found: " + forward.getFoundCycles() + backward.getFoundCycles());
            System.out.println("Closest to Target Match Count Forward:  " + forward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
//            System.out.println("Closest to Target Match Count Backward:  " + backward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
//            System.out.println("Total savable forward positions: " + forward.getSequencesToSave().size());
            System.out.println("Best Match: " + forward.getClosestToTarget());
            System.out.println("Target positions: " + Arrays.toString(forward.getTargetPositions()));
            System.out.println();
            System.out.println("Current moves: " + forward.getMoveList().stream().map(Move::getName).toList());
        }
//        }

//
//        try {
//            backwardThread.join();
////            Thread.sleep(20000L);
//        } catch (InterruptedException e) {
//            System.out.println("Main thread interrupted.");
//        }
//        System.out.println("backward thread done.");
//        forward.setTargetFoundHashes(backward.getFoundHashes());
//        forwardThread.start();
////        backwardThread.start();
//        while (forward.getSolutionFound() == null) {
//            Map<Long, List<Move>> recentMap = backward.getMostRecentFoundHashes();
//            if (recentMap != null) {
//                forward.setTargetFoundHashes(backward.getMostRecentFoundHashes());
//            }
//            try {
//                Thread.sleep(20000L);
//            } catch (InterruptedException e) {
//                System.out.println("Main thread interrupted.");
//            }
//            System.out.println();
//            Long duration = System.currentTimeMillis() - startTime;
//            System.out.println("Now searching for " + duration / 1000.0 + " seconds");
//            System.out.println("Forward explored: " + forward.getCount());
//            System.out.println("Backward explored: " + backward.getCount());
//            System.out.println("Cycles found: " + forward.getFoundCycles() + backward.getFoundCycles());
//            System.out.println("Closest to Target Match Count Forward:  " + forward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
//            System.out.println("Closest to Target Match Count Backward:  " + backward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
//            System.out.println("Total savable forward positions: " + forward.getSequencesToSave().size());

//        }
//        System.out.println();
//        System.out.println("Solution found!");
//        System.out.println();
//        System.out.println("Try: " + forward.getSolutionFound().stream()
//                .map(m -> m.getName())
//                .reduce("", (a, b) -> a.concat(".").concat(b)));
    }

    public static void runTest(PermutationChecker forward, PermutationChecker backward, PuzzleInfo puzzleInfo) {
        forward.printBoard();
        for (Move move : puzzleInfo.getAllowedMoves()) {
            for (Move move2 : puzzleInfo.getAllowedMoves()) {


                System.out.println("Performing move " + move);
                forward.transform(move);
                forward.printBoard();
                System.out.println("Performing move " + move2);
                forward.transform(move2);
                forward.printBoard();
                System.out.println("Inverted move list : " + forward.getInvertedMoveList());
                System.out.println("Stepping back with " + move2.getInverse());
                forward.transform(move2.getInverse());
                forward.printBoard();
                System.out.println("Stepping back with " + move.getInverse());
                forward.transform(move.getInverse());
                forward.printBoard();
            }
        }

    }
}
