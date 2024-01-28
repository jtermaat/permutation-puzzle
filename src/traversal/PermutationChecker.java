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
//import java.util.*;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.stream.Collectors;
//import java.util.stream.IntStream;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class PermutationChecker {
//    protected int numTargets;
//    protected int[] positions;
//    protected int[] matchesWithTargetCount;
//    protected boolean[][][] targetAllowedPositions;
////    protected Deque<Move> moves;
//    protected Deque<Integer> moveIndexes;
//    protected int moveIndex;
////    protected List<Move> allowedMoves;
//    protected Move[] allowedMoves;
//    protected int maxDepth;
//    protected List<PermutationBookmark> finalSequencesToSave;
//    List<PermutationBookmark> sequencesToSave;
//    protected PermutationBookmark[] closestToTarget;
//    protected int[] closestTargetCount;
//    protected long count;
//    protected int[] wildcards;
//
//    protected List<Puzzle> puzzles;
//    protected PuzzleInfo puzzleInfo;
//    protected long gameHash;
//
//
//
//    public PermutationChecker(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
//        this.puzzles = puzzles;
//        this.puzzleInfo = puzzleInfo;
//        this.numTargets = puzzles.size();
//        this.positions = new int[puzzles.get(0).getInitialState().length];
//        System.arraycopy(puzzles.get(0).getInitialState(), 0, this.positions, 0, positions.length);
//        this.maxDepth = maxDepth;
////        this.moves = new ArrayDeque<>();
//        this.moveIndexes = new ArrayDeque<>();
//        sequencesToSave = new ArrayList<>();
//        this.targetAllowedPositions = new boolean[numTargets][positions.length][positions.length];
//        for (int i = 0;i<numTargets;i++) {
//            this.targetAllowedPositions[i] = puzzles.get(i).getSolutionState();
//        }
//        closestToTarget = new PermutationBookmark[numTargets];
//        closestTargetCount = new int[numTargets];
//        this.allowedMoves = puzzleInfo.getAllowedMoves();
//        this.matchesWithTargetCount = new int[numTargets];
//        this.wildcards = new int[numTargets];
//        for (int t = 0;t<numTargets;t++) {
//            for (int i = 0; i < positions.length; i++) {
//                if (targetAllowedPositions[t][i][positions[i]]) {
//                    ++matchesWithTargetCount[t];
//                }
//            }
//            this.wildcards[t] = puzzles.get(t).getNumWildcards();
//        }
//        this.calculateGameHash();
//    }
//
//    protected void calculateGameHash() {
//        gameHash = 0L;
//        for (int i = 0;i<positions.length;++i) {
//            gameHash += (long)Math.pow(2, i) * positions[i];
//        }
//    }
//
//
//    protected void recordCountChanges(int index, int newValue) {
//        for (int t = 0;t<numTargets;++t) {
//            if (targetAllowedPositions[t][index][positions[index]] && !targetAllowedPositions[t][index][newValue]) {
//                --matchesWithTargetCount[t];
//            } else if (!targetAllowedPositions[t][index][positions[index]] && targetAllowedPositions[t][index][newValue]) {
//                ++matchesWithTargetCount[t];
//            }
//        }
//        this.gameHash += (long)Math.pow(2, index) * newValue - (long)Math.pow(2, index) * positions[index];
//    }
//
//    public void performSearch() {
//        gatherDataAndRemoveDuplicates(IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
//                .map(index -> new PermutationChecker(puzzles, puzzleInfo, maxDepth).searchWithMove(index))
//                .toList());
//    }
//
//    protected void gatherDataAndRemoveDuplicates(List<PermutationChecker> finishedCheckers) {
////        int minTargetsHit = positions.length;
//        List<PermutationBookmark[]> allClosestToTargets = finishedCheckers.stream()
//                .map(PermutationChecker::getClosestToTarget)
//                .toList();
//        this.closestToTarget = new PermutationBookmark[numTargets];
//        for (PermutationBookmark[] closest : allClosestToTargets) {
//            for (int i = 0;i<closest.length;++i) {
//                if (this.closestToTarget[i] == null || this.closestToTarget[i].getMatchesWithTargetCount() < closest[i].getMatchesWithTargetCount()) {
//                    this.closestToTarget[i] = closest[i];
//                }
//            }
//        }
//    }
//
//    public PermutationChecker searchWithMove(int index) {
//        if (moveIndexes.isEmpty() || !(allowedMoves[index].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
//            this.transform(allowedMoves[index]);
//            moveIndex = 0;
//            moveIndexes.push(index);
//            handleSaving();
//            while (performStep());
//            this.transform(allowedMoves[moveIndexes.pop()].getInverse());
//        }
//        return this;
//    }
//
//    protected boolean performStep() {
//        while (moveIndex == allowedMoves.length) {
//            if (moveIndexes.size() <= 1) {
//                return false;
//            } else {
//                moveIndex = moveIndexes.pop();
//                this.transform(allowedMoves[moveIndex].getInverse());
//                ++moveIndex;
//            }
//        }
//        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse()))) {
//            this.transform(allowedMoves[moveIndex]);
//            handleSaving();
//            if (moveIndexes.size() < maxDepth) {
//                moveIndexes.push(moveIndex);
//                moveIndex = 0;
//            } else {
//                this.transform(allowedMoves[moveIndex].getInverse());
//                ++moveIndex;
//            }
//        } else {
//            ++moveIndex;
//        }
//        return true;
//    }
//
//    public void transform(final Move move) {
//        short lastIndex = -1;
//        short lastValue = -1;
//        final short[][] swaps = move.getSwaps();
//        for (short i = 0;i < swaps.length;++i) {
//            if (lastIndex != swaps[i][1]) {
//                lastIndex = swaps[i][0];
//                lastValue = positions[swaps[i][0]];
//                recordCountChanges(swaps[i][0], positions[swaps[i][1]]);
//                positions[swaps[i][0]] = positions[swaps[i][1]];
//            } else {
//                final short temp = positions[swaps[i][0]];
//                recordCountChanges(swaps[i][0], lastValue);
//                positions[swaps[i][0]] = lastValue;
//                lastIndex = swaps[i][0];
//                lastValue = temp;
//            }
//        }
//    }
//
//    public List<Move> getMoveList() {
//        return moveIndexes.reversed().stream()
//                .map(i -> allowedMoves[i])
//                .collect(Collectors.toList());
//    }
//
//    protected void handleSaving() {
//        for (int t = 0;t<numTargets;++t) {
//            if (this.matchesWithTargetCount[t] > closestTargetCount[t] ||
//                    closestToTarget[t] == null ||
//                    (this.matchesWithTargetCount[t] == closestTargetCount[t] &&
//                                    this.moveIndexes.size() < closestToTarget[t].getMoves().size())) {
//                final int[] savePositions = new int[positions.length];
//                System.arraycopy(positions, 0, savePositions, 0, positions.length);
//                closestToTarget[t] = PermutationBookmark.builder()
//                        .id((int)count)
//                        .moves(getMoveList())
//                        .positions(savePositions)
//                        .matchesWithTargetCount(this.matchesWithTargetCount[t])
//                        .build();
//                closestTargetCount[t] = matchesWithTargetCount[t];
//            }
//        }
//    }
//
////    public void printBoard() {
////        System.out.print(positions[0]);
////        for (int i = 1;i<positions.length;i++) {
////            System.out.print(","+positions[i]);
////        }
////        System.out.println();
////    }
//}
