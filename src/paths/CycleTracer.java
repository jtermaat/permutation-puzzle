package paths;

import model.Move;
import model.RelativeCubeMove;

import java.util.Collections;
import java.util.List;

public class CycleTracer {
    private final Move[][][] allowedMoves;
    private final MoveNode startNode;
    private MoveNode currentNode;
    private final AbstractCubeCycle cycle;
    private int cycleIndex;
    private final int cycleStartIndex;
    List<Shortcut> foundShortcuts;
    private final int increment;

    public CycleTracer(MoveNode node, AbstractCubeCycle cycle, int cycleIndex, Move[][][] allowedMoves, List<Shortcut> foundShortcuts) {
        this.startNode = node;
        this.currentNode = node;
        this.cycle = cycle;
        this.cycleStartIndex = cycleIndex;
        this.cycleIndex = cycleIndex;
        this.allowedMoves = allowedMoves;
        this.foundShortcuts = foundShortcuts;
        boolean forward = (node.getMove().getInversionNumber() > 0)^(cycle.getMoves().get(cycleIndex).getInversionNumber() > 0);
        this.increment = forward ? 1 : -1;
    }

    public CycleTracer processNext() {
        Move move1 = currentNode.getMove();
        Move move2 = currentNode.getNext().getMove();
        RelativeCubeMove relativeMove = cycle.getMoves().get(cycleIndex);
        if (relativeMove.matches(move1, move2, cycle.isInvertedFront())) {
            currentNode = currentNode.getNext();
            cycleIndex += increment;
            cycleIndex = Math.floorMod(cycleIndex, cycle.getMoves().size()); // % cycle.getMoves().size();
            if (cycleIndex == cycleStartIndex) {
                foundShortcuts.add(new Shortcut(startNode, currentNode, Collections.emptyList()));
                return null;
            }
        } else {
            if (((cycleIndex - cycleStartIndex) * increment) % cycle.getMoves().size() > cycle.getMoves().size() / 2.0) {
//                foundShortcuts.add(new Shortcut(startNode, currentNode,
//                        cycle.getShortcutFor(cycleStartIndex, cycleIndex, startNode.getMove())));
                cycle.getShortcutFor(cycleStartIndex, startNode.getMove(),
                        ((cycleIndex - cycleStartIndex) * increment) % cycle.getMoves().size());
//                cycle.getPath(move2, )
                return null;
            }
        }
        return this;
    }
}
