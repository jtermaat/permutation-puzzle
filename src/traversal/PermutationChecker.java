package traversal;

import exception.CompletedTraversalException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.PermutationEntity;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermutationChecker implements Runnable {
    protected int[] positions;
    protected int matchesWithTargetCount;
    protected int[] targetPositions;
    protected long gameHash;
    protected Deque<Integer> moveIndexes;
    protected int moveIndex;
    protected List<Move> allowedMoves;
    protected int maxDepth;
    protected List<PermutationEntity> sequencesToSave;
    protected PermutationEntity closestToTarget;
    protected int closestTargetCount;
    protected int foundCycles;
    protected int depthChangeInterval;
    protected long count;
    protected int wildcards;

    protected final static int MAX_CHANGES = 100;
    protected final static int HASH_BASE = 2;


    public PermutationChecker(int[] positions, int[] targetPositions, List<Move> allowedMoves, int maxDepth, int depthChangeInterval, int wildcards) {
        this.positions = new int[positions.length];
        System.arraycopy(positions, 0, this.positions, 0, positions.length);
        gameHash = calculateGameHash();
        this.moveIndex = 0;
        this.maxDepth = maxDepth;
        this.depthChangeInterval = depthChangeInterval;
        this.moveIndexes = new ConcurrentLinkedDeque<>();// ArrayDeque
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
//        gameHash += (long) newValue * (int)Math.pow(HASH_BASE, index)
//                - (long) positions[index] * (int)Math.pow(HASH_BASE, index);
    }

    protected boolean stepForward() throws ArrayIndexOutOfBoundsException {
//        System.out.println("Tranforming move : " + allowedMoves.get(moveIndex));
        this.transform(allowedMoves.get(moveIndex));
        ++count;
        moveIndexes.push(moveIndex);
        moveIndex = 0;
//        System.out.println("New inverted move list: " + this.getInvertedMoveList().stream()
//                .map(s -> s.getName()).collect(Collectors.toList()));
        return moveIndexes.size() < maxDepth;
    }

    public void performNextMove() {
        try {
            if (!moveIndexes.isEmpty() && allowedMoves.get(moveIndex).equals(allowedMoves.get(moveIndexes.peek()).getInverse())) {
                ++moveIndex;
            }
            if (!stepForward()) {
                this.backtrack();
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            this.backtrack();
        }
    }

    protected void reset() {
        this.moveIndex = 0;
        this.foundCycles = 0;
        this.count = 0;
        this.sequencesToSave = null; // TODO: Save the data
    }

    protected void backtrack() {
        try {
            int lastMove = moveIndexes.pop();
            this.transform(allowedMoves.get(lastMove).getInverse());
            moveIndex = lastMove + 1;
        } catch (EmptyStackException | NoSuchElementException e) {
            throw new CompletedTraversalException("Finished this search after checking " + count + " permutations");
        }
    }
    public void transform(Move move) {
//        System.out.println("Old hash: " + gameHash);
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
//        System.out.println("New hash: " + gameHash);
    }

    public List<Move> getMoveList() {
        return moveIndexes.reversed().stream() // Reversed bc ArrayDeque streams like a stack
                .map(i -> allowedMoves.get(i))
                .collect(Collectors.toList());
    }

    public List<Move> getInvertedMoveList() {
        return moveIndexes.stream()
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
            hash += (long) positions[i] * (int)Math.pow(HASH_BASE, i);
        }
        return hash;
    }

    public void printBoard() {
        System.out.print(positions[0]);
        for (int i = 1;i<positions.length;i++) {
            System.out.print(","+positions[i]);
        }
        System.out.println();
    }

    @Override
    public void run() {
        try {
        while (true) {
//            try {
                performNextMove();
            }
        }
            catch (Exception e) {
                e.printStackTrace();
                System.out.println("Finished traversal at depth " + this.maxDepth);
                this.maxDepth += depthChangeInterval;
//                this.reset();
//                System.out.println("Starting traversal at depth " + this.maxDepth);
            }
//        }
    }
}
