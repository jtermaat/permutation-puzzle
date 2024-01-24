package traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.PermutationBookmark;
import model.Puzzle;
import model.PuzzleInfo;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermutationChecker {
    protected int numTargets;
    protected int[] positions;
    protected int[] matchesWithTargetCount;
    protected boolean[][][] targetAllowedPositions;
//    protected Deque<Move> moves;
    protected Deque<Integer> moveIndexes;
    protected int moveIndex;
//    protected List<Move> allowedMoves;
    protected Move[] allowedMoves;
    protected int maxDepth;
    protected List<PermutationBookmark> finalSequencesToSave;
    List<PermutationBookmark> sequencesToSave;
    protected PermutationBookmark[] closestToTarget;
    protected int[] closestTargetCount;
    protected long count;
    protected int[] wildcards;
    protected int changesCount;
    protected int maxChanges;

    protected List<Puzzle> puzzles;
    protected PuzzleInfo puzzleInfo;



    public PermutationChecker(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth, int maxChanges) {
        this.puzzles = puzzles;
        this.puzzleInfo = puzzleInfo;
        this.numTargets = puzzles.size();
        this.positions = new int[puzzles.get(0).getInitialState().length];
        System.arraycopy(puzzles.get(0).getInitialState(), 0, this.positions, 0, positions.length);
        this.maxDepth = maxDepth;
//        this.moves = new ArrayDeque<>();
        this.moveIndexes = new ArrayDeque<>();
        sequencesToSave = new ArrayList<>();
        this.targetAllowedPositions = new boolean[numTargets][positions.length][positions.length];
        for (int i = 0;i<numTargets;i++) {
            this.targetAllowedPositions[i] = puzzles.get(i).getSolutionState();
        }
        closestToTarget = new PermutationBookmark[numTargets];
        closestTargetCount = new int[numTargets];
        this.allowedMoves = puzzleInfo.getAllowedMoves();
        this.matchesWithTargetCount = new int[numTargets];
        this.wildcards = new int[numTargets];
        for (int t = 0;t<numTargets;t++) {
            for (int i = 0; i < positions.length; i++) {
                if (targetAllowedPositions[t][i][positions[i]]) {
                    ++matchesWithTargetCount[t];
                }
            }
            this.wildcards[t] = puzzles.get(t).getNumWildcards();
        }
        this.maxChanges = maxChanges;
    }

    protected int calculateMaxChanges() {
        int minClosestTargetCount = closestTargetCount[0];
        for (int i = 1;i<closestTargetCount.length;++i) {
            if (closestTargetCount[i] < minClosestTargetCount) {
                minClosestTargetCount = closestTargetCount[i];
            }
        }
        return positions.length - minClosestTargetCount;
    }


    protected void recordCountChanges(int index, int newValue) {
        for (int t = 0;t<numTargets;++t) {
            if (targetAllowedPositions[t][index][positions[index]] && !targetAllowedPositions[t][index][newValue]) {
                --matchesWithTargetCount[t];
            } else if (!targetAllowedPositions[t][index][positions[index]] && targetAllowedPositions[t][index][newValue]) {
                ++matchesWithTargetCount[t];
            }
        }
        if (positions[index] == index) {
            ++changesCount;
        } else if (newValue == index) {
            --changesCount;
        }
    }

    public void performSearch() {
        gatherDataAndRemoveDuplicates(IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
                .map(index -> new PermutationChecker(puzzles, puzzleInfo, maxDepth, maxChanges).searchWithMove(index))
                .toList());
    }

    protected void gatherDataAndRemoveDuplicates(List<PermutationChecker> finishedCheckers) {
//        int minTargetsHit = positions.length;
        List<PermutationBookmark[]> allClosestToTargets = finishedCheckers.stream()
                .map(PermutationChecker::getClosestToTarget)
                .toList();
        this.closestToTarget = new PermutationBookmark[numTargets];
        for (PermutationBookmark[] closest : allClosestToTargets) {
            for (int i = 0;i<closest.length;++i) {
                if (this.closestToTarget[i] == null || this.closestToTarget[i].getMatchesWithTargetCount() < closest[i].getMatchesWithTargetCount()) {
                    this.closestToTarget[i] = closest[i];
                }
            }
        }
//        for (int i = 0;i < numTargets;++i) {
//            minTargetsHit = Math.min(this.closestToTarget[i].getMatchesWithTargetCount(), minTargetsHit);
//        }
        List<PermutationBookmark> sortedSavedEntities = finishedCheckers.stream()
                .flatMap(p -> p.getSequencesToSave().stream())
                .sorted()
                .toList();
        System.out.println("Filtering " + sortedSavedEntities.size() + " saved sequences.");
        this.finalSequencesToSave = new ArrayList<>();
        PermutationBookmark currentEntity = null;
//        System.out.println("Min targets Hit: " + minTargetsHit);
        for (int i = 0;i<sortedSavedEntities.size();++i) {
            if (!sortedSavedEntities.get(i).equals(currentEntity)) {
                if (currentEntity != null && currentEntity.getChangesCount() < maxChanges) {
                    finalSequencesToSave.add(currentEntity);
                }
                currentEntity = sortedSavedEntities.get(i);
            } else {
                if (sortedSavedEntities.get(i).getMoves().size() < currentEntity.getMoves().size()) {
                    currentEntity = sortedSavedEntities.get(i);
                }
            }
        }
        this.sequencesToSave = finalSequencesToSave;
        System.out.println("Gathered " + this.sequencesToSave.size() + " filtered sequences to evaluate with all Threads.");

    }

    public PermutationChecker searchWithMove(int index) {
        if (moveIndexes.isEmpty() || !(allowedMoves[index].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
            this.transform(allowedMoves[index]);
            moveIndex = 0;
            moveIndexes.push(index);
            handleSaving();
            while (performStep());
            this.transform(allowedMoves[moveIndexes.pop()].getInverse());
        }
        return this;
    }

    protected boolean performStep() {
        while (moveIndex == allowedMoves.length) {
            if (moveIndexes.size() <= 1) {
                return false;
            } else {
                moveIndex = moveIndexes.pop();
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
            }
        }
        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
            this.transform(allowedMoves[moveIndex]);
            handleSaving();
            if (moveIndexes.size() < maxDepth) {
                moveIndexes.push(moveIndex);
                moveIndex = 0;
            } else {
                this.transform(allowedMoves[moveIndex].getInverse());
                ++moveIndex;
            }
        } else {
            ++moveIndex;
        }
        return true;
    }

    public void transform(final Move move) {
        int lastIndex = -1;
        int lastValue = -1;
        final int[][] swaps = move.getSwaps();
        for (int i = 0;i < swaps.length;++i) {
            if (lastIndex != swaps[i][1]) {
                lastIndex = swaps[i][0];
                lastValue = positions[swaps[i][0]];
                recordCountChanges(swaps[i][0], positions[swaps[i][1]]);
                positions[swaps[i][0]] = positions[swaps[i][1]];
            } else {
                final int temp = positions[swaps[i][0]];
                recordCountChanges(swaps[i][0], lastValue);
                positions[swaps[i][0]] = lastValue;
                lastIndex = swaps[i][0];
                lastValue = temp;
            }
        }
    }

    public List<Move> getMoveList() {
        return moveIndexes.reversed().stream()
                .map(i -> allowedMoves[i])
                .collect(Collectors.toList());
    }

    protected void handleSaving() {
        boolean gotCloserToTargets = false;
        for (int t = 0;t<numTargets;++t) {
            if (this.matchesWithTargetCount[t] > closestTargetCount[t] ||
                    closestToTarget[t] == null ||
                    (this.matchesWithTargetCount[t] == closestTargetCount[t] &&
                                    this.moveIndexes.size() < closestToTarget[t].getMoves().size())) {
                int[] savePositions = new int[positions.length];
                System.arraycopy(positions, 0, savePositions, 0, positions.length);
                closestToTarget[t] = PermutationBookmark.builder()
                        .id((int)count)
                        .moves(getMoveList())
                        .positions(savePositions)
                        .matchesWithTargetCount(this.matchesWithTargetCount[t])
                        .build();
                closestTargetCount[t] = matchesWithTargetCount[t];
                gotCloserToTargets = true;
            }
        }
        if (gotCloserToTargets) {
            maxChanges = calculateMaxChanges();
        }
        if (changesCount < maxChanges) {
                int[] savePositions = new int[positions.length];
                System.arraycopy(positions, 0, savePositions, 0, positions.length);
                sequencesToSave.add(PermutationBookmark.builder()
                        .moves(getMoveList())
                        .positions(savePositions)
                        .changesCount(this.changesCount)
                        .id(sequencesToSave.size())
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
