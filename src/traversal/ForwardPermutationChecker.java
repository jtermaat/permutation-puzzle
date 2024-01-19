package traversal;

import lombok.Data;
import model.Move;
import model.PermutationEntity;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class ForwardPermutationChecker extends PermutationChecker implements Runnable{
    protected int changesCount;
    protected Map<Long, Integer> foundHashes;
    protected Map<Long, List<Move>> targetFoundHashes;
    protected List<Move> solutionFound;


    public ForwardPermutationChecker(int[] positions, int[] targetPositions, List<Move> allowedMoves, int maxDepth, int depthChangeInterval, int wildcards, Map<Long, Integer> foundHashes, Map<Long, List<Move>> targetFoundHashes) {
        super(positions, targetPositions, allowedMoves, maxDepth, depthChangeInterval, wildcards);
        this.foundHashes = foundHashes;
        this.targetFoundHashes = targetFoundHashes;
    }

    @Override
    protected void recordCountChanges(int index, int newValue) {
        super.recordCountChanges(index, newValue);
        if (positions[index] == index && newValue != index) {
            ++changesCount;
        } else if (positions[index] != index && newValue == index) {
            --changesCount;
        }
    }

    @Override
    protected boolean stepForward() {
        boolean superStepForward = super.stepForward();

//        List<Move> targetSolution = targetFoundHashes.get(this.gameHash);
//        if (targetSolution != null && solutionWorks(targetSolution)) {
//            this.solutionFound = Stream.concat(moveIndexes.stream()
//                            .map(i -> allowedMoves.get(i)), targetSolution.stream())
//                    .collect(Collectors.toList());
//            System.out.println("Found solution!!!: " + this.solutionFound.stream()
//                    .map(Move::getName).reduce("", (a, b) -> a.concat(".").concat(b)));
//        }
//        Integer foundHashesBestMoveCount = foundHashes.get(this.gameHash);
//        if (foundHashesBestMoveCount != null && foundHashesBestMoveCount < moveIndexes.size()) {
//            ++foundCycles;
//            return false;
//        }
//        foundHashes.put(this.gameHash, moveIndexes.size());
        handleSaving();
        return superStepForward;
    }

    protected boolean solutionWorks(List<Move> solution) {
        int missCount = 0;
//        System.out.println("Testing a potential solution: " + solution.stream()
//                .map(s -> s.getName())
//                .reduce("", (a,b) -> a.concat(",").concat(b)));
        solution.stream().forEach(this::transform);
        for (int i = 0;i<positions.length;i++) {
            if (positions[i] != targetPositions[i]) {
                ++missCount;
            }
        }
//        System.out.println("Misses: " + missCount);
        solution.reversed().stream().forEach(this::transform);

        return missCount <= wildcards;
    }

    @Override
    protected void handleSaving() {
        super.handleSaving();
//        if (changesCount <= MAX_CHANGES) {
//            int[] savePositions = new int[positions.length];
//            System.arraycopy(positions, 0, savePositions, 0, positions.length);
//            sequencesToSave.add(PermutationEntity.builder()
//                    .moves(getMoveList())
//                    .positions(savePositions)
//                    .matchesWithTargetCount(this.matchesWithTargetCount)
//                    .changesCount(this.changesCount)
//                    .build());
//        }
    }

    @Override
    protected void reset() {
        super.reset();
        System.out.println("Resetting forward hashes.");
        System.out.println("Positions: " + Arrays.stream(positions)
                .mapToObj(i -> Integer.valueOf(i).toString())
                .reduce("", (a, b) -> a.concat(",").concat(b)));
        this.changesCount = 0;
        this.foundHashes = new HashMap<>();
    }
}
