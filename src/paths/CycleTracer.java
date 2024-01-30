package paths;

import model.Move;

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
        int faceChange = currentNode.getNext().getMove().getFace() - currentNode.getMove().getFace() % allowedMoves.length;
        int numberChange = currentNode.getNext().getMove().getNumber() - currentNode.getMove().getNumber();
        int inversionNumberChange = Math.abs(currentNode.getNext().getMove().getInversionNumber() - currentNode.getMove().getInversionNumber());
        if (cycle.getMoves().get(cycleIndex).getNumberOffset() == numberChange
                && cycle.getMoves().get(cycleIndex).getFaceOffset() == faceChange
                && cycle.getMoves().get(cycleIndex).getInversionNumber() == inversionNumberChange) {
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
