package traversal;

import exception.CompletedTraversalException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.PermutationEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public abstract class PermutationChecker implements Runnable {
    protected int[] positions;
    protected int matchesWithTargetCount;
    protected int[] targetPositions;
    protected long gameHash;
    protected Stack<Integer> moveIndexes;
    protected int moveIndex;
    protected List<Move> allowedMoves;
    protected int maxDepth;
    protected List<PermutationEntity> sequencesToSave;
    protected PermutationEntity closestToTarget;
    protected int closestTargetCount;
    protected int foundCycles;
    protected int depthChangeInterval;
    protected int count;
    protected int wildcards;

    protected final static int MAX_CHANGES = 100;


    public PermutationChecker(int[] positions, int[] targetPositions, List<Move> allowedMoves, int maxDepth, int depthChangeInterval, int wildcards) {
        this.positions = positions;
        gameHash = calculateGameHash();
        this.moveIndex = 0;
        this.maxDepth = maxDepth;
        this.moveIndexes = new Stack<>();
        this.foundCycles = 0;
        sequencesToSave = new ArrayList<>();
        this.targetPositions = targetPositions;
        this.allowedMoves = allowedMoves;
        this.matchesWithTargetCount = 0;
        for (int i = 0;i<targetPositions.length;i++) {
            if (targetPositions[i] == i) {
                ++matchesWithTargetCount;
            }
        }
        this.wildcards = wildcards;
    }

    protected void recordCountChanges(int index, int newValue) {
        if (positions[index] == targetPositions[index]) {
            --matchesWithTargetCount;
        } else if (newValue == targetPositions[index]) {
            ++matchesWithTargetCount;
        }
        gameHash += (long) newValue * (int)Math.pow(31, index)
                - (long) positions[index] * (int)Math.pow(31, index);
    }

    protected boolean stepForward() throws ArrayIndexOutOfBoundsException {
        this.transform(allowedMoves.get(moveIndex));
        ++count;
        moveIndexes.push(moveIndex);
        moveIndex = 0;
        return moveIndexes.size() < maxDepth;
    }

    public void performNextMove() {
        if (allowedMoves.get(moveIndex).equals(allowedMoves.get(moveIndexes.peek()).getInverse())) {
            ++moveIndex;
        }
        try {
            if (!stepForward()) {
                this.backtrack();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            this.backtrack();
        }
    }

    protected void backtrack() {
        try {
            int lastMove = moveIndexes.pop();
            this.transform(allowedMoves.get(lastMove).getInverse());
            moveIndex = lastMove + 1;
        } catch (NullPointerException e) {
            throw new CompletedTraversalException("Finished after checking " + count + " permutations");
        }
    }
    public void transform(Move move) {
        int lastIndex = -1;
        int lastValue = -1;
        for (int[] swap : move.getSwaps()) {
            if (lastIndex != swap[1]) {
                lastIndex = swap[0];
                lastValue = positions[swap[0]];
                recordCountChanges(swap[0], positions[swap[1]]);
                positions[swap[0]] = positions[swap[1]];
            } else {
                final int temp = positions[swap[0]];
                recordCountChanges(swap[0], lastValue);
                positions[swap[0]] = lastValue;
                lastIndex = swap[0];
                lastValue = temp;
            }
        }
    }

    protected List<Move> getMoveList() {
        return moveIndexes.stream()
                .map(i -> allowedMoves.get(i))
                .collect(Collectors.toList());
    }

    protected List<Move> getInvertedMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves.get(i).getInverse())
                .collect(Collectors.toList());
    }

    protected void handleSaving() {
        if (this.matchesWithTargetCount > closestTargetCount) {
            int[] savePositions = new int[positions.length];
            System.arraycopy(positions, 0, savePositions, 0, positions.length);
            closestToTarget = PermutationEntity.builder()
                    .moves(getMoveList())
                    .positions(savePositions)
                    .matchesWithTargetCount(this.matchesWithTargetCount)
                    .build();
            closestTargetCount = matchesWithTargetCount;
        }
    }

    private Long calculateGameHash() {
        Long hash = 0L;
        for (int i = 0;i<positions.length;++i) {
            hash += (long) positions[i] * (int)Math.pow(31, i);
        }
        return hash;
    }

    @Override
    public void run() {
        try {
            while (true) {
                performNextMove();
            }
        } catch (CompletedTraversalException c) {
            System.out.println(c.getMessage());
        }
    }
}
