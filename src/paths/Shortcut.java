package paths;

import lombok.Data;
import model.Move;
import paths.MoveNode;
import main.PuzzleSolver;

import java.util.List;

@Data
public class Shortcut {
    private final MoveNode start;
    private final MoveNode end;
    private final List<Move> shortcutMoves;

    public Shortcut(MoveNode start, MoveNode end, List<Move> shortcutMoves) {
        this.start = start;
        this.end = end;
        this.shortcutMoves = shortcutMoves;
    }

    public void print() {
        System.out.print("Original path: ");
        start.printPathTo(end);
        System.out.println();
        System.out.println("Shortcut moves: " + shortcutMoves.stream()
                .map(Move::getName)
                .reduce("", (a, b) -> a.concat(".").concat(b)));
        System.out.println();
    }

    public void activate() {
        start.createChainTo(end, shortcutMoves);
    }

    public void validate() {
        List<Move> originalMoves = start.toList(end);
        PuzzleSolver.validateEquality(originalMoves, shortcutMoves);
    }
}
