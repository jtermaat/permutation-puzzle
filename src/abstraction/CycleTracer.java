package abstraction;

import model.Move;
import paths.MoveNode;
import paths.Shortcut;

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
//        this.cycleStartIndex = cycleIndex;
//        this.cycleIndex = cycleIndex;
        this.allowedMoves = allowedMoves;
        this.foundShortcuts = foundShortcuts;
        boolean forward = (node.getMove().getInversionNumber() > 0)^(cycle.getMoves().get(cycleIndex).getInversionNumber() > 0);
//        if (forward) {
////            this.cycleIndex = cycleIndex + 1;
////            this.cycleStartIndex = cycleIndex + 1;
//        } else {
//            this.cycleIndex = cycleIndex;
//            this.cycleStartIndex = cycleIndex;
//        }
        this.cycleIndex = cycleIndex;
        this.cycleStartIndex = cycleIndex;
        this.increment = forward ? 1 : -1;
    }

    public CycleTracer processNext() {
        Move move1 = currentNode.getMove();
        Move move2 = currentNode.getNext().getMove();
        RelativeCubeMove relativeMove = cycle.getMoves().get(Math.floorMod(cycleIndex, cycle.getMoves().size()));
        if (relativeMove.matches(move1, move2, increment > 0)) {
            currentNode = currentNode.getNext();
            cycleIndex += increment;
//            cycleIndex = Math.floorMod(cycleIndex, cycle.getMoves().size()); // % cycle.getMoves().size();
            if (Math.floorMod(cycleIndex, cycle.getMoves().size()) == cycleStartIndex) {
                foundShortcuts.add(new Shortcut(startNode, currentNode, Collections.emptyList()));
                return null;
            }
        } else {
//            if (Math.floorMod((cycleIndex - cycleStartIndex) * increment, cycle.getMoves().size()) > 3) {
//                System.out.println("got here.");
//            }
            if ((cycleIndex - cycleStartIndex) * increment > cycle.getMoves().size() / 2.0) {
//                foundShortcuts.add(new Shortcut(startNode, currentNode,
//                        cycle.getShortcutFor(cycleStartIndex, cycleIndex, startNode.getMove())));
                List<Move> moves = cycle.getShortcutFor(cycleStartIndex, startNode.getMove(),
                        (cycleIndex-cycleStartIndex)*increment );
                System.out.println("match length is " + (cycleIndex-cycleStartIndex) * increment + " and cycle length is " + cycle.getMoves().size() + " so we got " + moves.size() + " moves.");
                System.out.println("Double checking length: " + startNode.toList(currentNode).size());
                Shortcut newShortcut = new Shortcut(startNode, currentNode, moves);
                foundShortcuts.add(newShortcut);
//                cycle.getPath(move2, )
                return null;
            }
        }
        return this;
    }
}
