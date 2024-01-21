package traversal;

import lombok.Data;
import model.Move;
import model.PermutationEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class SolutionChaser extends PermutationChecker {

    private List<List<PermutationEntity>> solutions;

    @Override
    public void performSearch() {
        solutions = new ArrayList<>();
        for (int targetNum = 0;targetNum < numTargets;++targetNum) {
            System.arraycopy(closestToTarget[targetNum].getPositions(), 0, positions, 0, positions.length);
            List<PermutationEntity> thisSolution = new ArrayList<>();
            thisSolution.add(closestToTarget[targetNum]);
            while (positions.length - closestToTarget[targetNum].getMatchesWithTargetCount() > wildcards[targetNum]) {
                thisSolution.add(getNextTransitionGreedy(targetNum));
            }
            solutions.add(thisSolution);
        }
    }

    protected PermutationEntity getNextTransitionGreedy(final int targetNum) {
        int parallelChasersCount = 128;
        List<List<Move>> moveLists = new ArrayList<>();
        for (int i = 1;i<allowedMoves.size();++i) {
            moveLists.add(allowedMoves.subList((i-1)*parallelChasersCount, i*parallelChasersCount));
        }
        List<PermutationEntity> bestTransitions = moveLists.parallelStream()
                .map(l -> getNextTransitionGreedyFromMoves(l, targetNum))
                .toList();
        PermutationEntity best = bestTransitions.get(0);
        for (int i = 1;i<bestTransitions.size();i++) {
            if (bestTransitions.get(i).getMatchesWithTargetCount() > best.getMatchesWithTargetCount()) {
                best = bestTransitions.get(i);
            }
        }
        return best;
    }

    protected PermutationEntity getNextTransitionGreedyFromMoves(List<Move> moves, final int targetNum) {
        moves.forEach(move -> {
           this.transform(move);
           checkPermutationEntity(targetNum);
           this.transform(move.getInverse());
        });
        return this.closestToTarget[targetNum];
    }


    protected void checkPermutationEntity(int targetNum) {
            if (this.matchesWithTargetCount[targetNum] > closestTargetCount[targetNum] ||
                    (this.matchesWithTargetCount[targetNum] == closestTargetCount[targetNum] &&
                            (closestToTarget[targetNum] == null))) {
                int[] savePositions = new int[positions.length];
                System.arraycopy(positions, 0, savePositions, 0, positions.length);
                closestToTarget[targetNum] = PermutationEntity.builder()
                        .moves(getMoveList())
                        .positions(savePositions)
                        .matchesWithTargetCount(this.matchesWithTargetCount[targetNum])
                        .build();
                closestTargetCount[targetNum] = matchesWithTargetCount[targetNum];
            }
    }
}
