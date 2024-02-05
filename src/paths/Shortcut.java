package paths;

import lombok.Data;
import model.Move;

import java.util.List;
import java.util.stream.Collectors;

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
        System.out.println("Shortcut moves: " +
                String.join(".", shortcutMoves.stream().map(Move::getName).collect(Collectors.toList())));
        System.out.println();
    }

    public void activate() {
        start.createChainTo(end, shortcutMoves);
    }
}
