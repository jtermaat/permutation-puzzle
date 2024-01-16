package main;

import model.Move;
import model.Puzzle;
import model.PuzzleInfo;
import traversal.BackwardPermutationChecker;
import traversal.ForwardPermutationChecker;

import java.sql.Date;
import java.sql.Time;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Driver {
    public static void main(String[] args) {
        String puzzlesFilename = "/Users/johntermaat/Downloads/puzzles.csv";
        String puzzleInfosFilename = "/Users/johntermaat/Downloads/puzzle_info.csv";
        List<Puzzle> puzzles = Puzzle.readPuzzleList(puzzlesFilename);
        List<PuzzleInfo> puzzleInfos = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename);
        Map<String, PuzzleInfo> puzzleInfoMap = PuzzleInfo.readPuzzleInfoList(puzzleInfosFilename).stream()
                .collect(Collectors.toMap(p -> p.getPuzzleType(), Function.identity()));
        int testPuzzleId = 20;
        Puzzle puzzleToUse = puzzles.stream()
                .filter(p -> p.getId() == testPuzzleId)
                .findFirst().get();
        PuzzleInfo puzzleInfoToUse = puzzleInfoMap.get(puzzleToUse.getPuzzleType());
        Map<Long, Integer> forwardCycleMap = new ConcurrentHashMap<>();
        Map<Long, List<Move>> backwardTrailMap = new ConcurrentHashMap<>();
        BackwardPermutationChecker backward = new BackwardPermutationChecker(puzzleToUse.getInitialState(),
                puzzleToUse.getSolutionState(),
                puzzleInfoToUse.getAllowedMoves(),
                5,
                (int)(100.0 / puzzleInfoToUse.getAllowedMoves().size()),
                puzzleToUse.getNumWildcards(),
                backwardTrailMap);
        ForwardPermutationChecker forward = new ForwardPermutationChecker(puzzleToUse.getInitialState(),
                puzzleToUse.getSolutionState(),
                puzzleInfoToUse.getAllowedMoves(),
                5,
                (int)(100.0 / puzzleInfoToUse.getAllowedMoves().size()),
                puzzleToUse.getNumWildcards(),
                forwardCycleMap,
                backwardTrailMap);
        Thread forwardThread = new Thread(forward);
        Thread backwardThread = new Thread(backward);
        Long startTime = System.currentTimeMillis();
        while (forward.getSolutionFound() == null) {
            try {
                Thread.sleep(20000L);
            } catch (InterruptedException e) {
                System.out.println("Main thread interrupted.");
            }
            System.out.println();
            Duration duration = Duration.between(new Date(startTime).toInstant() , new Date(System.currentTimeMillis()).toInstant());
            System.out.println("Now searching for " + duration.toString());
            System.out.println("Forward explored: " + forward.getCount());
            System.out.println("Backward explored: " + backward.getCount());
            System.out.println("Cycles found: " + forward.getFoundCycles() + backward.getFoundCycles());
            System.out.println("Closest to Target Match Count Forward:  " + forward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
            System.out.println("Closest to Target Match Count Backward:  " + backward.getClosestTargetCount() + " out of " + puzzleToUse.getInitialState().length);
            System.out.println("Total savable forward positions: " + forward.getSequencesToSave().size());

        }
        System.out.println();
        System.out.println("Solution found!");
        System.out.println();
        System.out.println("Try: " + forward.getSolutionFound().stream()
                .map(m -> m.getName())
                .reduce("", (a, b) -> a.concat(".").concat(b)));
    }
}
