package abstraction;

import model.Puzzle;
import paths.MoveNode;
import paths.Shortcut;

import java.util.*;

public class AbstractCycleChecker {
    private final List<AbstractCubeCycle> cycles;
    MoveNode startNode;
    private String cycleFileName;

    public AbstractCycleChecker(List<AbstractCubeCycle> cycles, Puzzle puzzle) {
        this.startNode = puzzle.getSolution();
        this.cycles = cycles;
    }

    public List<Shortcut> setupShortcuts() {
        List<Shortcut> foundShortcuts = new ArrayList<>();
        MoveNode node = startNode;
        while (node != null) {
            List<CycleTracer2> allTracers = new ArrayList<>();
            for (AbstractCubeCycle cycle : cycles) {
                allTracers.addAll(cycle.getCycleTracers(node, foundShortcuts));
            }
            allTracers.forEach(CycleTracer2::process);
            node = node.getNext();
        }
        return foundShortcuts;
    }
}
