package model;

import java.util.Map;
import java.util.List;

public class BackwardPermutationChecker extends PermutationChecker implements Runnable {
    protected Map<Long, List<Move>> foundHashes;

    public BackwardPermutationChecker(int cubeLength, int[] positions, int maxDepth, Map<Long, List<Move>> foundHashes) {
        super(cubeLength, positions, maxDepth);
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
        super.handleSaving();
        return superStepForward;
    }
}
