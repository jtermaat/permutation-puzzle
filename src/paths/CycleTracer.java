package paths;

import model.Move;
import model.RelativeMove;

import java.util.Collections;
import java.util.List;

public class CycleTracer {
    private final Move[][][] allowedMoves;
    private final MoveNode startNode;
    private MoveNode currentNode;
    private final AbstractCycle cycle;
    private int cycleIndex;
    private final int cycleStartIndex;
    List<Shortcut> foundShortcuts;

    public CycleTracer(MoveNode node, AbstractCycle cycle, int cycleIndex, Move[][][] allowedMoves, List<Shortcut> foundShortcuts) {
        this.startNode = node;
        this.currentNode = node;
        this.cycle = cycle;
        this.cycleStartIndex = cycleIndex;
        this.cycleIndex = cycleIndex;
        this.allowedMoves = allowedMoves;
        this.foundShortcuts = foundShortcuts;
    }

    public CycleTracer processNext() {
        Move move1 = currentNode.getMove();
        Move move2 = currentNode.getNext().getMove();
        RelativeMove relativeMove = cycle.getMoves().get(cycleIndex);
        if (relativeMove.matches(move1, move2, cycle.isInvertedFront())) {
            currentNode = currentNode.getNext();
            ++cycleIndex;
            cycleIndex = cycleIndex % cycle.getMoves().size();
            if (cycleIndex == cycleStartIndex) {
                foundShortcuts.add(new Shortcut(startNode, currentNode, Collections.emptyList()));
                return null;
            }
        } else {
            if (cycleIndex - cycleStartIndex % cycle.getMoves().size() > cycle.getMoves().size() / 2.0) {
                foundShortcuts.add(new Shortcut(startNode, currentNode, cycle.getShortcutFor(cycleStartIndex, cycleIndex, startNode.getMove())));
                return null;
            }
        }
        return this;
    }
}
