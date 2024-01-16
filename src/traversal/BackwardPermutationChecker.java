package traversal;

import model.Move;

import java.util.Map;
import java.util.List;

public class BackwardPermutationChecker extends PermutationChecker implements Runnable {
    protected Map<Long, List<Move>> foundHashes;

    public BackwardPermutationChecker(int[] positions, int[] targetPositions, List<Move> allowedMoves, int maxDepth, int depthChangeInterval, int wildcards, Map<Long, List<Move>> foundHashes) {
        super(positions, targetPositions, allowedMoves, maxDepth, depthChangeInterval, wildcards);
        this.foundHashes = foundHashes;
    }

    @Override
    protected boolean stepForward() {
        boolean superStepForward = super.stepForward();
        List<Move> foundHashesBestMoveList = foundHashes.get(this.gameHash);
        if (foundHashesBestMoveList != null && (foundHashesBestMoveList.size() < moveIndexes.size() || (foundHashesBestMoveList.size() == moveIndexes.size() && maxDepth - moveIndexes.size() > depthChangeInterval))) {
            ++foundCycles;
            return false;
        }
        foundHashes.put(this.gameHash, getInvertedMoveList());
        handleSaving();
        return superStepForward;
    }
}
