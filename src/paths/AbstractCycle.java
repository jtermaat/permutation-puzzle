package paths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import main.PuzzleSolver;
import model.Move;
import model.RelativeMove;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
public class AbstractCycle implements Comparable<AbstractCycle> {
    private List<RelativeMove> moves;
    private Move[][][] allowedMoves;

    public AbstractCycle(Cycle cycle, Move[][][] allowedMoves) {
        this.allowedMoves = allowedMoves;
        if (cycle == null || cycle.getMoves() == null || cycle.getMoves().isEmpty()) {
            moves = Collections.emptyList();
        } else {
            moves = new ArrayList<>(cycle.getMoves().size());
            for (int i = 0; i < cycle.getMoves().size(); ++i) {
                int lastIndex = Math.floorMod((i-1),cycle.getMoves().size());
                moves.add(new RelativeMove(Math.floorMod((cycle.getMoves().get(i).getFace() - cycle.getMoves().get(lastIndex).getFace()), allowedMoves.length),
                        cycle.getMoves().get(i).getNumber() - cycle.getMoves().get(lastIndex).getNumber(),
                        Math.floorMod((cycle.getMoves().get(i).getInversionNumber() - cycle.getMoves().get(lastIndex).getInversionNumber()), 2),
                        allowedMoves));
            }
        }
    }

    public Cycle getRelativeTo(Move startMove) {
        List<Move> moveList = new ArrayList<>();
        Move lastMove = startMove;
        moveList.add(lastMove);
        for (int i = 1;i<moves.size();i++) {
            lastMove = allowedMoves[Math.floorMod((moves.get(i).getFaceOffset() + lastMove.getFace()), allowedMoves.length)]
                                    [moves.get(i).getNumberOffset() + lastMove.getNumber()]
                                    [Math.floorMod((moves.get(i).getInversionNumber() + lastMove.getInversionNumber()), 2)];
            moveList.add(lastMove);
        }
        return new Cycle(moveList);
    }

    public Set<Move> getValidStartMoves() {
        Set<Move> validMoves = new HashSet<>();
        for (int i = 0;i<allowedMoves.length;++i) {
            for (int j = 0;j<allowedMoves[i].length;++j) {
                for (int k = 0;k<allowedMoves[i][j].length;++k) {
                    Cycle cycle = getRelativeTo(allowedMoves[i][j][k]);
                    if (cycle != null
                            && PuzzleSolver.validateEquality(getRelativeTo(allowedMoves[i][j][k]).getMoves(), Collections.emptyList())) {
                        validMoves.add(allowedMoves[i][j][k]);
                    }
                }
            }
        }
        return validMoves;
    }

    public List<Move> getShortcutFor(int startIndex, int endIndex, Move startMove) {
        return null; //TODO: Implement
    }

//    public void cubeSortMoves() {
//        int currentFace = -1;
//        List<List<Move>> groupedList = new ArrayList<>();
//        List<Move> currentList = new ArrayList<>();
//        for (int i = 0;i<moves.size();++i) {
//            if (moves.get(i).getFace() == currentFace) {
//                currentList.add(moves.get(i));
//            } else {
//                currentList.sort(Comparator.comparing(Move::getNumber));
//                groupedList.add(currentList);
//                currentFace = moves.get(i).getFace();
//                currentList = new ArrayList<>();
//                currentList.add(moves.get(i));
//            }
//        }
//        moves = new ArrayList<>();
//        for (List<Move> list : groupedList) {
//            moves.addAll(list);
//        }
////        pivotToLowestComparisonScore();
//        System.out.println("Validating...");
//        if (isValid()) {
//            System.out.println("Valid cycle! ");
//        } else {
//            System.out.println("NOT A VALID CYCLE");
//        }
//        print();
//    }
//
//    public void runValidation() {
//        System.out.println("Validating...");
//        if (isValid()) {
//            System.out.println("Valid cycle! ");
//        } else {
//            System.out.println("NOT A VALID CYCLE");
//        }
//        print();
//    }
//
//    private void print() {
//        System.out.println(this.toString());
//    }
//
//    private void pivotToLowestComparisonScore() {
//        List<Move> allMoveTypes = new HashSet<>(moves).stream().toList();
//        Map<Move, Integer> allMoveMap = new HashMap<>();
//        for (int i = 0;i<allMoveTypes.size();++i) {
//            allMoveMap.put(allMoveTypes.get(i), i);
//        }
//
//        long minScore = Long.MAX_VALUE;
//        int minScoreIndex = -1;
//        for (int startIndex = 0;startIndex < moves.size();++startIndex) {
//            long score = 0L;
//            for (int i = 0;i<moves.size();++i) {
//                score += allMoveMap.get(moves.get((i+startIndex)%moves.size())) * (long)Math.pow(moves.size(), i);
//            }
//            if (score < minScore) {
//                minScore = score;
//                minScoreIndex = startIndex;
//            }
//        }
//        pivot(minScoreIndex);
//    }
//
//    private void pivot(int index) {
//        List<Move> newList = new ArrayList<>(moves.size());
//        for (int i = 0;i<moves.size();++i) {
//            newList.add(moves.get((index + i) % moves.size()));
//        }
//        moves = newList;
//    }
//
//    @Override
//    public String toString() {
//        if (moves.size() > 0) {
//            StringBuilder sb = new StringBuilder();
//            sb.append(moves.get(0).getName());
//            for (int i = 1; i < moves.size(); ++i) {
//                sb.append(".").append(moves.get(i).getName());
//            }
//            sb.append("\n");
//            return sb.toString();
//        }
//        return "";
//    }
//
    @Override
    public int compareTo(AbstractCycle o) {
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
        AbstractCycle otherCycle = (AbstractCycle) other;
        if (otherCycle.getMoves().size() != moves.size()) {
            return false;
        }
//        System.out.println("Testing equality of " );
        System.out.println(this.toString());
        System.out.println(other.toString());
        System.out.println();
        for (int i = 0;i<moves.size();++i) {
            if (matchesStartingAtIndex(otherCycle, i)) {
//                System.out.println("They match with index " + i + "!");
                return true;
            }
        }
//        System.out.println("They don't match.");
        return false;

    }

    private boolean matchesStartingAtIndex(AbstractCycle other, int index) {
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
                .map(RelativeMove::toString)
                .reduce("", (a, b) -> a.concat(",").concat(b)).concat("\n");
    }
}