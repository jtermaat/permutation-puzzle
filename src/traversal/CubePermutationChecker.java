package traversal;

import lombok.Builder;
import lombok.Data;
import model.Move;
import model.PermutationBookmark;
import model.Puzzle;
import model.PuzzleInfo;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

@Data
public class CubePermutationChecker extends PermutationChecker {
    private Move secondToLastMove;

    public CubePermutationChecker(List<Puzzle> puzzles, PuzzleInfo puzzleInfo, int maxDepth) {
        super(puzzles, puzzleInfo, maxDepth);
    }

    public void performSearch() {
        gatherDataAndRemoveDuplicates(allowedMoves.parallelStream()
                .map(move -> new CubePermutationChecker(puzzles, puzzleInfo, maxDepth).searchWithMove(move))
                .toList());
    }

    @Override
    public PermutationChecker searchWithMove(Move move) {
        if (moves.isEmpty()
                || !(move.equals(moves.peek().getInverse())
                        || move.equals(moves.peek())
                        && (move.isInversion()
                            || move.equals(secondToLastMove)))) {
            this.transform(move);
            ++count;
            secondToLastMove = moves.peek();
            moves.push(move);
            handleSaving();
            if (moves.size() < maxDepth) {
                allowedMoves.forEach(this::searchWithMove);
            }
            this.transform(moves.pop().getInverse());
            if (moves.size() > 1) {
                Move lastMove = moves.pop();
                secondToLastMove = moves.peek();
                moves.push(lastMove);
            }
        }
        return this;
    }

}
