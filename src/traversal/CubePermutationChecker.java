//package traversal;
//
//import lombok.Builder;
//import lombok.Data;
//import model.Move;
//import model.PermutationBookmark;
//import model.Puzzle;
//import model.PuzzleInfo;

//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.IntStream;
//
//@Data
//public class CubePermutationChecker extends PermutationChecker {
//    private int secondToLastMove;
//
//    public CubePermutationChecker(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
//        super(puzzles, puzzleInfo, maxDepth);
//        secondToLastMove = -1;
//    }
//
//    @Override
//    public void performSearch() {
//        gatherDataAndRemoveDuplicates(IntStream.range(0, allowedMoves.length).boxed().toList().parallelStream()
//                .map(index -> new CubePermutationChecker(puzzles, puzzleInfo, maxDepth).searchWithMove(index))
//                .toList());
//    }
//
//    @Override
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
//        if (moveIndexes.isEmpty() || !(allowedMoves[moveIndex].equals(allowedMoves[moveIndexes.peek()].getInverse())
//                                        || moveIndex == moveIndexes.peek()
//                                        && (allowedMoves[moveIndex].isInversion()
//                                            || secondToLastMove == moveIndex))) {
//            this.transform(allowedMoves[moveIndex]);
//            handleSaving();
//            if (moveIndexes.size() < maxDepth) {
//                secondToLastMove = moveIndexes.peek();
//                moveIndexes.push(moveIndex);
//                moveIndex = 0;
//            } else {
//                this.transform(allowedMoves[moveIndex].getInverse());
//                int lastIndex = moveIndexes.pop();
//                secondToLastMove = moveIndexes.peek();
//                moveIndexes.push(lastIndex);
//                ++moveIndex;
//            }
//        } else {
//            ++moveIndex;
//        }
//        return true;
//    }
//
//}
