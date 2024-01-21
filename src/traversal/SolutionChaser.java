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
public class SolutionChaser extends PermutationChecker {

    private List<List<PermutationEntity>> solutions;
    private int targetNum;

    public SolutionChaser(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
        super(puzzles, puzzleInfo, maxDepth);
    }

    @Override
    public void performSearch() {
        solutions = new ArrayList<>();
        for (targetNum = 0;targetNum < numTargets;++targetNum) {
            if (positions.length - closestToTarget[targetNum].getMatchesWithTargetCount() <= wildcards[targetNum]) {
                solutions.add(Collections.singletonList(closestToTarget[targetNum]));
                continue;
            }
            System.arraycopy(closestToTarget[targetNum].getPositions(), 0, positions, 0, positions.length);
            List<PermutationEntity> thisSolution = new ArrayList<>();
            thisSolution.add(closestToTarget[targetNum]);
            PermutationEntity next = null;
            while (positions.length - closestToTarget[targetNum].getMatchesWithTargetCount() > wildcards[targetNum]) {
                next = getNextTransitionGreedy(next);
                thisSolution.add(next);
            }
            solutions.add(thisSolution);
        }
    }

    @Override
    protected void recordCountChanges(int index, int newValue) {
        if (targetAllowedPositions[targetNum][index][positions[index]] && !targetAllowedPositions[targetNum][index][newValue]) {
            --matchesWithTargetCount[targetNum];
        } else if (!targetAllowedPositions[targetNum][index][positions[index]] && targetAllowedPositions[targetNum][index][newValue]) {
            ++matchesWithTargetCount[targetNum];
        }
    }

    protected PermutationEntity getNextTransitionGreedy(PermutationEntity last) {
        int parallelChasersCount = 128;
        List<List<Move>> moveLists = new ArrayList<>();
        for (int i = 1;i<allowedMoves.size();++i) {
            moveLists.add(allowedMoves.subList((i-1)*parallelChasersCount, i*parallelChasersCount));
        }
        List<PermutationEntity> bestTransitions = moveLists.parallelStream()
                .map(l -> duplicate(last).getNextTransitionGreedyFromMoves(l))
                .toList();
        PermutationEntity best = bestTransitions.get(0);
        for (int i = 1;i<bestTransitions.size();i++) {
            if (bestTransitions.get(i).getMatchesWithTargetCount() > best.getMatchesWithTargetCount()) {
                best = bestTransitions.get(i);
            }
        }
        return best;
    }

    public PermutationEntity getNextTransitionGreedyFromMoves(List<Move> moves) {
        moves.forEach(move -> {
           this.transform(move);
           checkPermutationEntity();
           this.transform(move.getInverse());
        });
        return this.closestToTarget[targetNum];
    }


    protected void checkPermutationEntity() {
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

    protected SolutionChaser duplicate(PermutationEntity lastPermutation) {
        SolutionChaser returnValue = new SolutionChaser(puzzles, puzzleInfo, maxDepth);
        if (lastPermutation != null) {
            returnValue.positions = new int[returnValue.positions.length];
            System.arraycopy(lastPermutation.getPositions(), 0, returnValue.positions, 0, returnValue.positions.length);
            returnValue.matchesWithTargetCount = new int[numTargets];
            for (int t = 0; t < numTargets; t++) {
                for (int i = 0; i < returnValue.positions.length; i++) {
                    if (targetAllowedPositions[t][i][positions[i]]) {
                        ++returnValue.matchesWithTargetCount[t];
                    }
                }
            }
        }
        return returnValue;
    }
}
