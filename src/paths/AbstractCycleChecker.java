package paths;

import model.Puzzle;
import java.util.*;

public class AbstractCycleChecker {
    private List<AbstractCubeCycle> cycles;
    MoveNode startNode;
    private String cycleFileName;

    public AbstractCycleChecker(List<AbstractCubeCycle> cycles, Puzzle puzzle) {
        this.startNode = puzzle.getSolution();
        this.cycles = cycles;
    }

    public List<Shortcut> setupShortcuts() {
        List<Shortcut> foundShortcuts = new ArrayList<>();
        MoveNode node = startNode;
        Set<CycleTracer> tracers = new HashSet<>();
        for (AbstractCubeCycle cycle : cycles) {
            tracers.addAll(cycle.getCycleTracers(node, foundShortcuts));
        }
        while (node.getNext() != null) {
            node = node.getNext();
            Set<CycleTracer> nextTracers = new HashSet<>();
            for (CycleTracer tracer : tracers) {
                nextTracers.add(tracer.processNext());
            }
            for (AbstractCubeCycle cycle : cycles) {
                nextTracers.addAll(cycle.getCycleTracers(node, foundShortcuts));
            }
            tracers = nextTracers;
        }
        return foundShortcuts;
    }
}
