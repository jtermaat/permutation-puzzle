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
//                moves.add(new RelativeMove(allowedMoves, cycle.getMoves().get(i), cycle.getMoves().get(lastIndex)));
//                moves.add(new RelativeMove(Math.floorMod((cycle.getMoves().get(i).getFace() - cycle.getMoves().get(lastIndex).getFace()), allowedMoves.length),
//                        cycle.getMoves().get(i).getNumber() - cycle.getMoves().get(lastIndex).getNumber(),
//                        Math.floorMod((cycle.getMoves().get(i).getInversionNumber() - cycle.getMoves().get(lastIndex).getInversionNumber()), 2),
//                        allowedMoves));
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

//            RelativeMove relativeMove = forward == !startMove.isInversion() ? moves.get(i) : moves.get(moves.size() - i);
//            RelativeMove relativeMove = (!forward)^startMove.isInversion() ?  moves.get(moves.size() - i) : moves.get(i);
            RelativeCubeMove relativeMove = forward ^startMove.isInversion() ? moves.get(Math.floorMod(i, moves.size())) : moves.get(Math.floorMod(moves.size() - (i-1), moves.size()));
//            RelativeMove relativeMove = moves.get(i);
            try {
                lastMove = relativeMove.getRelativeTo(lastMove, forward ^startMove.isInversion());
                moveList.add(lastMove);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("we got arrayIndex out of bounds trying to get cycle relative to " + startMove + " for " + this);
                System.out.println("List so far: " + moveList);
                System.out.println("Current relative move: " + relativeMove);
                return null;
            }
//            try {
//                lastMove = allowedMoves[Math.floorMod((moves.get(i).getFaceOffset() + lastMove.getFace()), allowedMoves.length)]
//                        [moves.get(i).getNumberOffset() + lastMove.getNumber()]
//                        [Math.floorMod((moves.get(i).getInversionNumber() + lastMove.getInversionNumber()), 2)];
//                moveList.add(lastMove);
//            } catch (ArrayIndexOutOfBoundsException e) {
//                return null;
//            }
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
                            } else {
//                                System.out.println("Starting with " + allowedMoves[j][k][l] + " we get FAILED " + getRelativeTo(allowedMoves[j][k][l]));
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
                        } else {
//                            System.out.println("Starting with " + move + " we get FAILED " + getRelativeTo(move));
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
//                            System.out.println("Cycle " + toString() + " is INVALID for starting move " + move);
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

//    public List<CycleTracer> getCycleTracers(MoveNode node, List<Shortcut> foundShortcuts) {
//        return IntStream.range(0, moves.size()).boxed()
//                .map(i -> new CycleTracer(node, this, i, allowedMoves, foundShortcuts))
//                .toList();
//    }

    public List<CycleTracer2> getCycleTracers(MoveNode node, List<Shortcut> foundShortcuts) {
        List<CycleTracer2> tracers = new ArrayList<>();
        for (int i = 0;i<moves.size();++i) {
            if (this.validStartMovesFromIndex.get(i).contains(node.getMove())) {
                tracers.add(new CycleTracer2(node, getRelativeTo(node.getMove(), i), foundShortcuts, this));
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
//        System.out.println("Testing equality.");
        AbstractCubeCycle otherCycle = (AbstractCubeCycle) other;
        if (otherCycle.getMoves().size() != moves.size()) {
            return false;
        }
//        System.out.println("Testing equality of " );
//        System.out.println(this.toString());
//        System.out.println(other.toString());
//        System.out.println();

        for (int i = 0;i<moves.size();++i) {
            if (matchesStartingAtIndex(otherCycle, i)) {
//                System.out.println("They match with index " + i + "!");
                return true;
            }
        }
//        System.out.println("They don't match.");
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