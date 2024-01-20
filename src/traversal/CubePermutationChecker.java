package traversal;

import model.Move;

public class CubePermutationChecker extends PermutationChecker {
    private Move secondToLastMove;

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
            Move lastMove = moves.pop();
            secondToLastMove = moves.peek();
            moves.push(lastMove);
        }
        return this;
    }

}
