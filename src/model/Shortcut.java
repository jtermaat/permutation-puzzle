package model;

import java.util.List;

public class Shortcut {
    private final MoveNode start;
    private final MoveNode end;
    private final List<Move> shortcutMoves;

    public Shortcut(MoveNode start, MoveNode end, List<Move> shortcutMoves) {
        this.start = start;
        this.end = end;
        this.shortcutMoves = shortcutMoves;
    }

    public void activate() {
        start.createChainTo(end, shortcutMoves);
    }
}
