package abstraction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import model.Move;
import paths.MoveNode;
import paths.Shortcut;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
public class AbstractCubeCycle implements Comparable<AbstractCubeCycle> {
    private List<RelativeCubeMove> moves;
    List<Set<Move>> validStartMovesFromIndex;
    private Move[][][] allowedMoves;
    private boolean forward;
    private boolean inverseForward;

    public AbstractCubeCycle(String string, Move[][][] allowedMoves) {
        this.allowedMoves = allowedMoves;
        String[] moveStrings = string.split(",");
        moves = new ArrayList<>(moveStrings.length);
        for (String str : moveStrings) {
            moves.add(new RelativeCubeMove(str, allowedMoves));
        }
        if (!moves.isEmpty()) {
            forward = moves.get(0).getInversionNumber() <= 0;
            inverseForward = moves.get(moves.size()-1).getInversionNumber() <= 0;
            initValidStartMoves();
        }
    }

    public AbstractCubeCycle(Cycle cycle, Move[][][] allowedMoves) {
        this.allowedMoves = allowedMoves;
        if (cycle == null || cycle.getMoves() == null || cycle.getMoves().isEmpty()) {
            moves = Collections.emptyList();
        } else {
            forward = !cycle.getMoves().get(0).isInversion();
            inverseForward = !cycle.getMoves().get(cycle.getMoves().size()-1).isInversion();
            moves = new ArrayList<>(cycle.getMoves().size());
            for (int i = 0; i < cycle.getMoves().size(); ++i) {
                int lastIndex = Math.floorMod((i-1),cycle.getMoves().size());
                moves.add(new RelativeCubeMove(allowedMoves, cycle.getMoves().get(lastIndex), cycle.getMoves().get(i)));
            }
        }
        initValidStartMoves();
    }

    public Cycle getRelativeTo(Move startMove) {
        return getRelativeTo(startMove, 0);
    }

    public Cycle getRelativeTo(Move startMove, int index) {
        List<Move> moveList = new ArrayList<>();
        Move lastMove = startMove;
        moveList.add(lastMove);
        for (int i = index+1;i<moves.size()+index;i++) {
            RelativeCubeMove relativeMove = forward ^startMove.isInversion() ? moves.get(Math.floorMod(i, moves.size())) : moves.get(Math.floorMod(moves.size() - (i-1), moves.size()));
            try {
                lastMove = relativeMove.getRelativeTo(lastMove, forward ^startMove.isInversion());
                moveList.add(lastMove);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("we got arrayIndex out of bounds trying to get cycle relative to " + startMove + " for " + this);
                System.out.println("List so far: " + moveList);
                System.out.println("Current relative move: " + relativeMove);
                return null;
            }
        }
        return new Cycle(moveList);
    }

    public void printEquivalentCycles() {
        for (Move move : getValidStartMoves()) {
            System.out.println(getRelativeTo(move));
        }
    }

    public void initValidStartMoves() {
        validStartMovesFromIndex = new ArrayList<>(moves.size());
        for (int i = 0;i<moves.size();++i) {
            Set<Move> validMoves = new HashSet<>();
            for (int j = 0;j<allowedMoves.length;++j) {
                for (int k = 0;k<allowedMoves[j].length;++k) {
                    for (int l = 0;l<allowedMoves[j][k].length;++l) {
                        Cycle cycle = getRelativeTo(allowedMoves[j][k][l], i);
                        if (cycle != null) {
                            if (Cycle.validateEquality(getRelativeTo(allowedMoves[j][k][l], i).getMoves(), Collections.emptyList())) {
                                validMoves.add(allowedMoves[j][k][l]);
                            }
                        }
                    }
                }
            }
            validStartMovesFromIndex.add(i, validMoves);
        }
    }

    public Set<Move> getValidStartMoves() {
        Set<Move> validMoves = new HashSet<>();
        for (Move[][] allowedMove : allowedMoves) {
            for (Move[] value : allowedMove) {
                for (Move move : value) {
                    Cycle cycle = getRelativeTo(move);
                    if (cycle != null) {
                        if (Cycle.validateEquality(getRelativeTo(move).getMoves(), Collections.emptyList())) {
                            validMoves.add(move);
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    public boolean isValidForFittingMoves() {
        for (Move[][] allowedMovesForFace : allowedMoves) {
            for (Move[] allowedMovesForFaceAndNumber : allowedMovesForFace) {
                for (Move move : allowedMovesForFaceAndNumber) {
                    Cycle cycle = getRelativeTo(move);
                    if (cycle != null) {
                        if (!Cycle.validateEquality(getRelativeTo(move).getMoves(), Collections.emptyList())) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public List<Move> getShortcutFor(int startIndex, Move startMove, int length) {
        Cycle cycle = getRelativeTo(startMove, startIndex);
        return cycle.getMoves().subList(length, cycle.getMoves().size()).stream()
                .map(Move::getInverse)
                .toList();
    }

    public List<CycleTracer> getCycleTracers(MoveNode node, List<Shortcut> foundShortcuts) {
        List<CycleTracer> tracers = new ArrayList<>();
        for (int i = 0;i<moves.size();++i) {
            if (this.validStartMovesFromIndex.get(i).contains(node.getMove())) {
                tracers.add(new CycleTracer(node, getRelativeTo(node.getMove(), i), foundShortcuts, this));
            }
        }
        return tracers;
    }

    @Override
    public int compareTo(AbstractCubeCycle o) {
        if (moves.size() < o.getMoves().size()) {
            return -1;
        } else if (moves.size() > o.getMoves().size()) {
            return 1;
        }
        for (int i = 0;i<moves.size();++i) {
            if (!moves.get(i).equals(o.getMoves().get(i))) {
                return moves.get(i).compareTo(o.getMoves().get(i));
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        AbstractCubeCycle otherCycle = (AbstractCubeCycle) other;
        if (otherCycle.getMoves().size() != moves.size()) {
            return false;
        }
        for (int i = 0;i<moves.size();++i) {
            if (matchesStartingAtIndex(otherCycle, i)) {
                return true;
            }
        }
        return false;

    }

    private boolean matchesStartingAtIndex(AbstractCubeCycle other, int index) {
        for (int i = 0;i<moves.size();++i) {
            if (!moves.get(i).equals(other.getMoves().get(Math.floorMod(i+index, moves.size())))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int code = 0;
        if (moves != null) {
            for (int i = 0; i < moves.size(); ++i) {
                code += moves.get(i).hashCode();
            }
        }
        return code;
    }

    @Override
    public String toString() {
        return moves.stream()
                .map(RelativeCubeMove::toString)
                .reduce("", (a, b) -> a.concat(",").concat(b)).concat("\n");
    }
}