package traversal;

import model.Move;
import model.PermutationEntity;

import java.util.List;
import java.util.Map;

public class ForwardPermutationChecker extends PermutationChecker implements Runnable{
    protected int changesCount;
    protected Map<Long, Integer> foundHashes;
    protected Map<Long, List<Move>> targetFoundHashes;

    public ForwardPermutationChecker(int cubeLength, int[] positions, int maxDepth, Map<Long, Integer> foundHashes, Map<Long, List<Move>> targetFoundHashes) {
        super(cubeLength, positions, maxDepth);
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
        Integer foundHashesBestMoveCount = foundHashes.get(this.gameHash);
        if (foundHashesBestMoveCount != null && (foundHashesBestMoveCount < moveIndexes.size() || (foundHashesBestMoveCount == moveIndexes.size() && maxDepth - moveIndexes.size() > depthChangeInterval))) {
            ++foundCycles;
            return false;
        }
        foundHashes.put(this.gameHash, moveIndexes.size());
        handleSaving(foundHashesBestMoveCount);
        return superStepForward;
    }

    protected void handleSaving(Integer foundHashesBestMoveCount) {
        if (foundHashesBestMoveCount == null || foundHashesBestMoveCount > moveIndexes.size()+1) {
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
            super.handleSaving();
        }
    }
}
