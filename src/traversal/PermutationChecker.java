package traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.PermutationEntity;
import model.Puzzle;
import model.PuzzleInfo;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermutationChecker {
    protected int[] positions;
    protected int matchesWithTargetCount;
    boolean[][] targetAllowedPositions;
//    protected Set<Integer>[] targetPositions;
    private Move secondToLastMove;
    protected Deque<Move> moves;
    protected List<Move> allowedMoves;
    protected int maxDepth;
    protected List<PermutationEntity> sequencesToSave;
    protected PermutationEntity closestToTarget;
    protected int closestTargetCount;
    protected long count;
    protected int wildcards;
    protected int changesCount;
    protected List<Move> solutionFound;

    protected boolean isCube;

    protected final static int MAX_CHANGES = 10;


    public PermutationChecker(Puzzle puzzle, PuzzleInfo puzzleInfo, int maxDepth) {
        this.positions = new int[positions.length];
        System.arraycopy(puzzle.getInitialState(), 0, this.positions, 0, positions.length);
        this.maxDepth = maxDepth;
        this.moves = new ArrayDeque<>();
        sequencesToSave = new ArrayList<>();
        this.targetAllowedPositions = puzzle.getSolutionState();
        this.allowedMoves = puzzleInfo.getAllowedMoves();
        this.matchesWithTargetCount = 0;
        for (int i = 0;i<positions.length;i++) {
            if (targetAllowedPositions[i][positions[i]]) {
                ++matchesWithTargetCount;
            }
        }
        this.wildcards = puzzle.getNumWildcards();
        this.isCube = puzzleInfo.getPuzzleType().toUpperCase().contains("CUBE");
    }

    protected PermutationChecker duplicateEmpty() {
        int[] positions = new int[this.positions.length];
        System.arraycopy(this.positions, 0, positions, 0, this.positions.length);
        return PermutationChecker.builder()
                .positions(positions)
                .moves(new ArrayDeque<>())
                .sequencesToSave(new ArrayList<>())
                .matchesWithTargetCount(this.matchesWithTargetCount)
                .targetAllowedPositions(this.targetAllowedPositions)
                .allowedMoves(allowedMoves)
                .maxDepth(maxDepth)
                .sequencesToSave(new ArrayList<>())
                .count(0)
                .wildcards(this.wildcards)
                .isCube(this.isCube)
                .build();
    }

    protected void recordCountChanges(int index, int newValue) {
        if (targetAllowedPositions[index][positions[index]] && !targetAllowedPositions[index][newValue]) {
            --matchesWithTargetCount;
        } else if (!targetAllowedPositions[index][positions[index]] && targetAllowedPositions[index][newValue]) {
            ++matchesWithTargetCount;
        }
        if (positions[index] == index) {
            ++changesCount;
        } else if (newValue == index) {
            --changesCount;
        }
    }

    public void performSearch() {
        List<PermutationChecker> finishedCheckers = allowedMoves.parallelStream()
                .map(move -> duplicateEmpty().searchWithMove(move))
                .toList();
    }

    public PermutationChecker searchWithMove(Move move) {
        if (moves.isEmpty()
                || !(move.equals(moves.peek().getInverse())
                    || (isCube // To prevent obvious cube cycles
                        && move.equals(moves.peek())
                        && (move.isInversion()
                            || move.equals(secondToLastMove))))) {
            this.transform(move);
            ++count;
            secondToLastMove = moves.peek();
            moves.push(move);
            handleSaving();
            if (moves.size() < maxDepth) {
                allowedMoves.forEach(this::searchWithMove);
            }
            this.transform(moves.pop().getInverse());
            Move lastMove = moves.pop();
            secondToLastMove = moves.peek();
            moves.push(lastMove);
        }
        return this;
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

    public List<Move> getMoveList() {
        return new ArrayList<>(moves.reversed());
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
        if (changesCount <= MAX_CHANGES) {
            int[] savePositions = new int[positions.length];
            System.arraycopy(positions, 0, savePositions, 0, positions.length);
            sequencesToSave.add(PermutationEntity.builder()
                    .moves(getMoveList())
                    .positions(savePositions)
                    .matchesWithTargetCount(this.matchesWithTargetCount)
                    .changesCount(this.changesCount)
                    .build());
        }
    }

//    public void printBoard() {
//        System.out.print(positions[0]);
//        for (int i = 1;i<positions.length;i++) {
//            System.out.print(","+positions[i]);
//        }
//        System.out.println();
//    }
}
