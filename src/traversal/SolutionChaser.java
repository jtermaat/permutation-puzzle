//package traversal;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import model.Move;
//import model.PermutationBookmark;
//import model.Puzzle;
//import model.PuzzleInfo;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class SolutionChaser extends PermutationChecker {
//
//    private List<List<PermutationBookmark>> solutions;
//    private int targetNum;
//
//    public SolutionChaser(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
//        super(puzzles, puzzleInfo, maxDepth);
//    }
//
//    public SolutionChaser(PermutationChecker checker, int maxDepth) {
//        super(checker.getPuzzles(), checker.getPuzzleInfo(), maxDepth);
//        this.closestToTarget = checker.getClosestToTarget();
//        this.closestTargetCount = checker.getClosestTargetCount();
//        this.allowedMoves = new Move[checker.getSequencesToSave().size()];
//        List<Move> allowedMovesList = checker.getSequencesToSave().stream()
//                .map(PermutationBookmark::toMove)
//                .toList();
//        for (int i = 0;i<allowedMovesList.size();++i) {
//            allowedMoves[i] = allowedMovesList.get(i);
//        }
//    }
//
//    public SolutionChaser(PermutationChecker checker, int maxDepth, Move[] allowedMoves) {
//        this(checker, maxDepth);
//        this.allowedMoves = allowedMoves;
//    }
//
//    @Override
//    public void performSearch() {
//        solutions = new ArrayList<>();
//        for (targetNum = 0;targetNum < numTargets;++targetNum) {
//            System.out.println("Hunting for solution " + targetNum);
//            System.arraycopy(closestToTarget[targetNum].getPositions(), 0, positions, 0, positions.length);
//            List<PermutationBookmark> thisSolution = new ArrayList<>();
//            PermutationBookmark next = closestToTarget[targetNum];
//            thisSolution.add(next);
//            int newDistance = positions.length - closestToTarget[targetNum].getMatchesWithTargetCount();
//            int lastDistance = positions.length;
//            while (newDistance > wildcards[targetNum] && newDistance != lastDistance) {
//                System.out.println("Current distance from solution is " + newDistance + " out of allowed " + wildcards[targetNum]);
//                next = getNextTransitionGreedy(next);
//                thisSolution.add(next);
//                lastDistance = newDistance;
//                newDistance = positions.length - closestToTarget[targetNum].getMatchesWithTargetCount();
//                if (newDistance == lastDistance) {
//                    System.out.println("Uh oh, new distance equals last distance, so we have a problem.");
//                }
//            }
//            if (newDistance <= wildcards[targetNum]) {
//                System.out.println("Solution Found for target " + targetNum + "!!!");
//                solutions.add(thisSolution);
//            } else {
//                System.out.println("No solution found for target " + targetNum);
//                System.out.println("Old distance: " + lastDistance);
//                System.out.println("New distance: " + newDistance);
//                solutions.add(new ArrayList<>());
//            }
//
//        }
//    }
//
//    public void writeSolutionsToFile(String filename) {
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
//            for (int i = 0;i<numTargets;++i) {
//                List<PermutationBookmark> solution = solutions.get(i);
//                List<Move> allMoves = solution.stream()
//                        .filter(Objects::nonNull)
//                        .flatMap(s -> s.getMoves().stream())
//                        .toList();
//                if (!allMoves.isEmpty()) {
//                    StringBuilder sb = new StringBuilder();
//                    sb.append(puzzles.get(i).getId()).append(",");
//                    sb.append(allMoves.get(0).getName());
//                    for (int j = 1; j < allMoves.size(); ++j) {
//                        sb.append(".").append(allMoves.get(j).getName());
//                    }
//                    sb.append("\n");
//                    System.out.println("For target " + i + ", writing: " + sb.toString());
//                    writer.write(sb.toString());
//                }
//            }
//            writer.flush();
//        } catch (IOException ie) {
//            System.out.println("Error writing results to file " + filename);
//            ie.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void recordCountChanges(int index, int newValue) {
//        if (targetAllowedPositions[targetNum][index][positions[index]] && !targetAllowedPositions[targetNum][index][newValue]) {
//            --matchesWithTargetCount[targetNum];
//        } else if (!targetAllowedPositions[targetNum][index][positions[index]] && targetAllowedPositions[targetNum][index][newValue]) {
//            ++matchesWithTargetCount[targetNum];
//        }
//    }
//
//    protected PermutationBookmark getNextTransitionGreedy(PermutationBookmark last) {
//        int parallelChasersCount = 128;
//        List<List<Move>> moveLists = new ArrayList<>();
//        List<Move> allowedMovesList = List.of(allowedMoves);
//        for (int i = 1;i<allowedMoves.length / parallelChasersCount;++i) {
//            moveLists.add(allowedMovesList.subList((i-1)*parallelChasersCount, i*parallelChasersCount));
//        }
//        List<PermutationBookmark> bestTransitions = moveLists.stream()
//                .map(s -> duplicate(last).getNextTransitionGreedyFromMoves(s))
//                .toList();
//        if (bestTransitions.isEmpty()) {
//            return null;
//        }
//        PermutationBookmark best = bestTransitions.get(0);
//        for (int i = 1;i<bestTransitions.size();i++) {
//            if (bestTransitions.get(i).getMatchesWithTargetCount() > best.getMatchesWithTargetCount()) {
//                best = bestTransitions.get(i);
//            }
//        }
//        return best;
//    }
//
//    public PermutationBookmark getNextTransitionGreedyFromMoves(List<Move> moves) {
////        System.out.println("Trying " + moves.size() + " moves out of " + allowedMoves.size() + " moves.");
//        moves.forEach(move -> {
//           this.transform(move);
//           --this.maxDepth;
//           checkPermutationEntity();
//           if (this.maxDepth > 0) {
//               getNextTransitionGreedyFromMoves(List.of(this.allowedMoves));
//           }
//           this.transform(move.getInverse());
//           ++this.maxDepth;
//        });
//        return this.closestToTarget[targetNum];
//    }
//
//
//    protected void checkPermutationEntity() {
//            if (this.matchesWithTargetCount[targetNum] > closestTargetCount[targetNum] ||
//                    (this.matchesWithTargetCount[targetNum] == closestTargetCount[targetNum] &&
//                            (closestToTarget[targetNum] == null))) {
//                int[] savePositions = new int[positions.length];
//                System.arraycopy(positions, 0, savePositions, 0, positions.length);
//                closestToTarget[targetNum] = PermutationBookmark.builder()
//                        .moves(getMoveList())
//                        .positions(savePositions)
//                        .matchesWithTargetCount(this.matchesWithTargetCount[targetNum])
//                        .build();
//                closestTargetCount[targetNum] = matchesWithTargetCount[targetNum];
//            }
//    }
//
//    protected SolutionChaser duplicate(PermutationBookmark lastPermutation) {
//        SolutionChaser returnValue = new SolutionChaser(this, this.maxDepth, this.allowedMoves);
//        if (lastPermutation != null) {
//            returnValue.positions = new int[returnValue.positions.length];
//            System.arraycopy(lastPermutation.getPositions(), 0, returnValue.positions, 0, returnValue.positions.length);
//            returnValue.matchesWithTargetCount = new int[numTargets];
//            for (int t = 0; t < numTargets; t++) {
//                for (int i = 0; i < returnValue.positions.length; i++) {
//                    if (targetAllowedPositions[t][i][positions[i]]) {
//                        ++returnValue.matchesWithTargetCount[t];
//                    }
//                }
//            }
//        }
//        return returnValue;
//    }
//}
