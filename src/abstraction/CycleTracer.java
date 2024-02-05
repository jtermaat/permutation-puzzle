package abstraction;

import model.Move;
import model.Puzzle;
import paths.MoveNode;
import paths.Shortcut;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CycleTracer {
    private final MoveNode startNode;
    private MoveNode currentNode;
    private final Cycle cycle;

    AbstractCubeCycle abstractCycle;
    List<Shortcut> foundShortcuts;

    public CycleTracer(MoveNode node, Cycle cycle, List<Shortcut> foundShortcuts, AbstractCubeCycle abstractCycle) {
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

                        List<Move> remainingInvertedMoves = Puzzle.reverse(cycle.getMoves().subList(i, cycle.getMoves().size()).stream())
                                .map(Move::getInverse)
                                .collect(Collectors.toList());
                        Shortcut newShortcut = new Shortcut(startNode, currentNode, remainingInvertedMoves);
                        foundShortcuts.add(newShortcut);
                    }
                    return;
                }
            } else {
                return;
            }
        }
        foundShortcuts.add(new Shortcut(startNode, currentNode, Collections.emptyList()));
    }
}

