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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermutationChecker {
    protected int numTargets;
    protected int[] positions;
    protected int[] matchesWithTargetCount;
    protected boolean[][][] targetAllowedPositions;
    protected Deque<Move> moves;
    protected List<Move> allowedMoves;
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



    public PermutationChecker(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
        this.puzzles = puzzles;
        this.puzzleInfo = puzzleInfo;
        this.numTargets = puzzles.size();
        this.positions = new int[positions.length];
        System.arraycopy(puzzles.get(0).getInitialState(), 0, this.positions, 0, positions.length);
        this.maxDepth = maxDepth;
        this.moves = new ArrayDeque<>();
        sequencesToSave = new ArrayList<>();
        this.targetAllowedPositions = new boolean[numTargets][positions.length][positions.length];
        for (int i = 0;i<numTargets;i++) {
            this.targetAllowedPositions[i] = puzzles.get(i).getSolutionState();
        }
        closestToTarget = new PermutationBookmark[numTargets];
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
        this.maxChanges = getMaxChanges();
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
                .count(0)
                .wildcards(this.wildcards)
                .maxChanges(this.maxChanges)
                .build();
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
        gatherDataAndRemoveDuplicates(allowedMoves.parallelStream()
                .map(move -> duplicateEmpty().searchWithMove(move))
                .toList());
    }

    protected void gatherDataAndRemoveDuplicates(List<PermutationChecker> finishedCheckers) {
        int minTargetsHit = 0;
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
        for (int i = 0;i < numTargets;++i) {
            minTargetsHit = Math.min(this.closestToTarget[i].getMatchesWithTargetCount(), minTargetsHit);
        }
        List<PermutationBookmark> sortedSavedEntities = finishedCheckers.stream()
                .flatMap(p -> p.getSequencesToSave().stream())
                .sorted()
                .toList();
        System.out.println("Filtering " + sortedSavedEntities + " saved sequences.");
        this.finalSequencesToSave = new ArrayList<>();
        PermutationBookmark currentEntity = null;
        for (int i = 0;i<sortedSavedEntities.size();++i) {
            if (!sortedSavedEntities.get(i).equals(currentEntity)) {
                if (currentEntity != null && currentEntity.getChangesCount() < (positions.length - minTargetsHit)) {
                    finalSequencesToSave.add(currentEntity);
                }
                currentEntity = sortedSavedEntities.get(i);
            } else {
                if (sortedSavedEntities.get(i).getMoves().size() < currentEntity.getMoves().size()) {
                    currentEntity = sortedSavedEntities.get(i);
                }
            }
        }
        System.out.println("Gathered " + this.sequencesToSave.size() + " filtered sequences to evaluate with all Threads.");

    }

    public PermutationChecker searchWithMove(Move move) {
        if (moves.isEmpty() || !(move.equals(moves.peek().getInverse()))) {
            this.transform(move);
            ++count;
            moves.push(move);
            handleSaving();
            if (moves.size() < maxDepth) {
                allowedMoves.forEach(this::searchWithMove);
            }
            this.transform(moves.pop().getInverse());
            Move lastMove = moves.pop();
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
        boolean gotCloserToTargets = false;
        for (int t = 0;t<numTargets;++t) {
            if (this.matchesWithTargetCount[t] > closestTargetCount[t] ||
                    (this.matchesWithTargetCount[t] == closestTargetCount[t] &&
                            (closestToTarget[t] == null ||
                                    this.moves.size() < closestToTarget[t].getMoves().size()))) {
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
        if (changesCount <= maxChanges) {
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
