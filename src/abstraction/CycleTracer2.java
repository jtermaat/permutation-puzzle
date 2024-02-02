package abstraction;

import model.Move;
import paths.MoveNode;
import paths.Shortcut;

import java.util.Collections;
import java.util.List;

public class CycleTracer2 {
    private final MoveNode startNode;
    private MoveNode currentNode;
    private final Cycle cycle;

    AbstractCubeCycle abstractCycle;
    List<Shortcut> foundShortcuts;

    public CycleTracer2(MoveNode node, Cycle cycle, List<Shortcut> foundShortcuts, AbstractCubeCycle abstractCycle) {
        this.startNode = node;
        this.currentNode = node;
        this.cycle = cycle;
        this.foundShortcuts = foundShortcuts;
        this.abstractCycle = abstractCycle;
    }

    public void process() {
        for (int i = 0;i<cycle.getMoves().size();++i) {
            if (currentNode != null) {
                if (currentNode.getMove().equals(cycle.getMoves().get(i))) {
                    currentNode = currentNode.getNext();
                } else {
                    if (i > cycle.getMoves().size() / 2.0) {
                        List<Move> remainingInvertedMoves = cycle.getMoves().subList(i, cycle.getMoves().size()).reversed().stream()
                                .map(Move::getInverse)
                                .toList();
                        Shortcut newShortcut = new Shortcut(startNode, currentNode, remainingInvertedMoves, abstractCycle);
                        foundShortcuts.add(newShortcut);
                    }
                    return;
                }
            } else {
                return;
            }
        }
        foundShortcuts.add(new Shortcut(startNode, currentNode, Collections.emptyList(), abstractCycle));
    }
}

