package traversal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.PermutationBookmark;
import model.Puzzle;
import model.PuzzleInfo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SolutionChaser extends PermutationChecker {

    private List<List<PermutationBookmark>> solutions;
    private int targetNum;

    public SolutionChaser(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
        super(puzzles, puzzleInfo, maxDepth);
    }

    public SolutionChaser(PermutationChecker checker) {
        super(checker.getPuzzles(), checker.getPuzzleInfo(), checker.maxDepth);
        this.closestToTarget = checker.getClosestToTarget();
        this.closestTargetCount = checker.getClosestTargetCount();
        this.allowedMoves = checker.getFinalSequencesToSave().stream()
                .map(PermutationBookmark::toMove)
                .toList();
    }

    @Override
    public void performSearch() {
        solutions = new ArrayList<>();
        for (targetNum = 0;targetNum < numTargets;++targetNum) {
            System.arraycopy(closestToTarget[targetNum].getPositions(), 0, positions, 0, positions.length);
            List<PermutationBookmark> thisSolution = new ArrayList<>();
            thisSolution.add(closestToTarget[targetNum]);
            PermutationBookmark next = closestToTarget[targetNum];
            thisSolution.add(next);
            while (positions.length - closestToTarget[targetNum].getMatchesWithTargetCount() > wildcards[targetNum]) {
                next = getNextTransitionGreedy(next);
                thisSolution.add(next);
            }
            solutions.add(thisSolution);
        }
    }

    public void writeSolutionsToFile(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (int i = 0;i<numTargets;++i) {
                List<PermutationBookmark> solution = solutions.get(i);
                StringBuilder sb = new StringBuilder();
                sb.append(puzzles.get(i).getId()).append(",");
                List<Move> allMoves = solution.stream()
                        .flatMap(s -> s.getMoves().stream())
                        .toList();
                sb.append(allMoves.get(0).getName());
                for (int j = 1;j<allMoves.size();++j) {
                    sb.append(".").append(allMoves.get(j).getName());
                }
                sb.append("\n");
                writer.write(sb.toString());
            }
        } catch (IOException ie) {
            System.out.println("Error writing results to file " + filename);
            ie.printStackTrace();
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

    protected PermutationBookmark getNextTransitionGreedy(PermutationBookmark last) {
        int parallelChasersCount = 128;
        List<List<Move>> moveLists = new ArrayList<>();
        for (int i = 1;i<allowedMoves.size();++i) {
            moveLists.add(allowedMoves.subList((i-1)*parallelChasersCount, i*parallelChasersCount));
        }
        List<PermutationBookmark> bestTransitions = moveLists.parallelStream()
                .map(l -> duplicate(last).getNextTransitionGreedyFromMoves(l))
                .toList();
        PermutationBookmark best = bestTransitions.get(0);
        for (int i = 1;i<bestTransitions.size();i++) {
            if (bestTransitions.get(i).getMatchesWithTargetCount() > best.getMatchesWithTargetCount()) {
                best = bestTransitions.get(i);
            }
        }
        return best;
    }

    public PermutationBookmark getNextTransitionGreedyFromMoves(List<Move> moves) {
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
                closestToTarget[targetNum] = PermutationBookmark.builder()
                        .moves(getMoveList())
                        .positions(savePositions)
                        .matchesWithTargetCount(this.matchesWithTargetCount[targetNum])
                        .build();
                closestTargetCount[targetNum] = matchesWithTargetCount[targetNum];
            }
    }

    protected SolutionChaser duplicate(PermutationBookmark lastPermutation) {
        SolutionChaser returnValue = new SolutionChaser(this);
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
