package paths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import main.PuzzleSolver;
import model.Move;
import traversal.Permutation;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class Cycle implements Comparable<Cycle> {
    private List<Move> moves;

    public Cycle(String serialized, List<Move> allMoves) {
        Map<String, Move> moveMap = allMoves.stream()
                .collect(Collectors.toMap(Move::getName, Function.identity()));
        String[] parts = serialized.split("\\.");
        moves = new ArrayList<>(parts.length);
        for (String part : parts) {
            moves.add(moveMap.get(part));
        }
    }

    public Cycle(List<Move> moves) {
        this.moves = new ArrayList<>(moves.size());
        this.moves.addAll(moves);
    }

    public Cycle(Shortcut shortcut) {
        List<Move> originalMoves = shortcut.getStart().toList(shortcut.getEnd());
        List<Move> completionMoves = shortcut.getShortcutMoves().reversed().stream()
                .map(Move::getInverse)
                .toList();
        moves = new ArrayList<>(originalMoves.size() + completionMoves.size());
        moves.addAll(originalMoves);
        moves.addAll(completionMoves);
    }

    private List<Cycle> splitIntoAllSubCycles() {
        Set<Cycle> stillSplittingSet = new HashSet<>();
        Set<Cycle> doneSet = new HashSet<>();
        stillSplittingSet.add(this);
        while (!stillSplittingSet.isEmpty()) {
            Set<Cycle> newStillSplittingSet = new HashSet<>();
            for (Cycle cycle : stillSplittingSet) {
                List<Cycle> splitList = cycle.splitIntoSubCycles();
                if (splitList.size() > 1) {
                    newStillSplittingSet.addAll(splitList);
                } else {
                    doneSet.addAll(splitList);
                }
            }
            stillSplittingSet = newStillSplittingSet;
        }
        return doneSet.stream().toList();
    }

    private List<Cycle> splitIntoSubCycles() {
        cubeSortMoves();
        if (!moves.isEmpty()) {
            Permutation testPermutation = new Permutation(moves.get(0).getNewPositions().length);
            for (int i = 0; i < moves.size(); ++i) {
                testPermutation.resetPositions();
                for (int j = i; j < moves.size(); ++j) {
                    testPermutation.transform(moves.get(j));
                    boolean matches = true;
                    for (int k = 0;k<testPermutation.getPositions().length;++k) {
                        matches = matches && testPermutation.getPositions()[k] == k;
                    }
                    if (matches && !(i == 0 && j+1 == moves.size())) {
                        return split(i, j+1);
                    }
                }
            }
//            int halfPoint = (int)(moves.size()/2.0);
//            for (int i = halfPoint+1;i<moves.size() + halfPoint;++i) {
//                testPermutation.resetPositions();
//                for (int j = i; j < moves.size() + halfPoint; ++j) {
//                    testPermutation.transform(moves.get(j%moves.size()));
//                    boolean matches = true;
//                    for (int k = 0;k<testPermutation.getPositions().length;++k) {
//                        matches = matches && testPermutation.getPositions()[k] == k;
//                    }
//                    if (matches && !(i == halfPoint && j+1 == moves.size() + halfPoint)) {
//                        return split(i, (j+1)%moves.size());
//                    }
//                }
//            }
        }
        System.out.println("Nothing to split in this: " + this.toString());
        return Collections.singletonList(this);
    }

    public List<Cycle> split(int startIndex, int endIndex) {
//        System.out.println("Splitting.");
//        print();
        List<Move> moveList1 = moves.subList(startIndex, endIndex);
        List<Move> moveList2 = new ArrayList<>(moves.size() - moveList1.size());
        moveList2.addAll(moves.subList(endIndex, moves.size()));
        moveList2.addAll(moves.subList(0, startIndex));
        Cycle newCycle1 = new Cycle(moveList1);
        newCycle1.cubeSortMoves();
        Cycle newCycle2 = new Cycle(moveList2);
        newCycle2.cubeSortMoves();
        System.out.println("Split into " + newCycle1 + " and " + newCycle2);
        return Arrays.asList(newCycle1, newCycle2);
    }

    public Cycle getInverse() {
        return Cycle.builder()
                .moves(moves.stream()
                        .map(Move::getInverse)
                        .toList())
                .build();
    }

//    public List<Shortcut> findShortcuts(MoveNode startNode) {
//        Map<>
//    }

    public boolean isValid() {
        return PuzzleSolver.validateEquality(moves, Collections.emptyList());
    }

    public static List<Move> cubeSortMoves(List<Move> moves) {
        if (moves.size() > 1) {
//            pivotToLowestComparisonScore();
//            System.out.println("Before cube sorting: ");
//            print();
            int currentFace = -1;
            List<List<Move>> groupedList = new ArrayList<>();
            List<Move> currentList = new ArrayList<>();
            int backIndex = moves.size() - 1;
            Move firstMove = moves.get(0);
            while (backIndex >= 0 && moves.get(backIndex).getFace() == firstMove.getFace()) {
                currentList.add(moves.get(backIndex));
                --backIndex;
            }
            for (int i = 0;i<=backIndex;++i) {
                if (moves.get(i).getFace() == currentFace) {
                    currentList.add(moves.get(i));
                } else {
                    currentList.sort(Comparator.comparing(Move::getInversionNumber));
                    currentList.sort(Comparator.comparing(Move::getNumber));
                    groupedList.add(currentList);
                    currentFace = moves.get(i).getFace();
                    currentList = new ArrayList<>();
                    currentList.add(moves.get(i));
                }
            }
            if (!currentList.isEmpty()) {
                currentList.sort(Comparator.comparing(Move::getInversionNumber));
                currentList.sort(Comparator.comparing(Move::getNumber));
                groupedList.add(currentList);
            }
            moves = new ArrayList<>();
            for (List<Move> list : groupedList) {
                moves.addAll(list);
            }
//            pivotToLowestComparisonScore();
//            System.out.println("Validating...");
//            if (isValid()) {
//                System.out.println("Valid cycle! ");
//            } else {
//                System.out.println("NOT A VALID CYCLE");
//            }
//            System.out.println("After cube sorting: ");
//            print();
//            System.out.println();
//            System.out.println();
        }
        return moves;
    }

    public void cubeSortMoves() {
        if (moves.size() > 1) {
//            pivotToLowestComparisonScore();
//            System.out.println("Before cube sorting: ");
//            print();
            int currentFace = -1;
            List<List<Move>> groupedList = new ArrayList<>();
            List<Move> currentList = new ArrayList<>();
            int backIndex = moves.size() - 1;
            Move firstMove = moves.get(0);
            while (backIndex >= 0 && moves.get(backIndex).getFace() == firstMove.getFace()) {
                currentList.add(moves.get(backIndex));
                --backIndex;
            }
            for (int i = 0;i<=backIndex;++i) {
                if (moves.get(i).getFace() == currentFace) {
                    currentList.add(moves.get(i));
                } else {
                    currentList.sort(Comparator.comparing(Move::getInversionNumber));
                    currentList.sort(Comparator.comparing(Move::getNumber));
                    groupedList.add(currentList);
                    currentFace = moves.get(i).getFace();
                    currentList = new ArrayList<>();
                    currentList.add(moves.get(i));
                }
            }
            if (!currentList.isEmpty()) {
                currentList.sort(Comparator.comparing(Move::getInversionNumber));
                currentList.sort(Comparator.comparing(Move::getNumber));
                groupedList.add(currentList);
            }
            moves = new ArrayList<>();
            for (List<Move> list : groupedList) {
                moves.addAll(list);
            }
//            pivotToLowestComparisonScore();
//            System.out.println("Validating...");
//            if (isValid()) {
//                System.out.println("Valid cycle! ");
//            } else {
//                System.out.println("NOT A VALID CYCLE");
//            }
//            System.out.println("After cube sorting: ");
//            print();
//            System.out.println();
//            System.out.println();
        }
    }

    public void runValidation() {
        System.out.println("Validating...");
        if (isValid()) {
            System.out.println("Valid cycle! ");
        } else {
            System.out.println("NOT A VALID CYCLE");
        }
        print();
    }

    private void print() {
        System.out.println(this.toString());
    }

    private void pivotToLowestComparisonScore() {
        List<Move> allMoveTypes = new HashSet<>(moves).stream()
                .sorted()
                .toList();
        Map<Move, Integer> allMoveMap = new HashMap<>();
        for (int i = 0;i<allMoveTypes.size();++i) {
            allMoveMap.put(allMoveTypes.get(i), i);
        }

        long minScore = Long.MAX_VALUE;
        int minScoreIndex = -1;
        for (int startIndex = 0;startIndex < moves.size();++startIndex) {
            long score = 0L;
            for (int i = 0;i<moves.size();++i) {
                score += allMoveMap.get(moves.get((i+startIndex)%moves.size())) * (long)Math.pow(allMoveTypes.size(), i);
            }
            if (score < minScore) {
                minScore = score;
                minScoreIndex = startIndex;
            }
        }
        pivot(minScoreIndex);
    }

    private void pivot(int index) {
        List<Move> newList = new ArrayList<>(moves.size());
        for (int i = 0;i<moves.size();++i) {
            newList.add(moves.get((index + i) % moves.size()));
        }
        moves = newList;
    }

    @Override
    public String toString() {
        if (!moves.isEmpty()) {
            return moves.stream()
                    .map(Move::getName)
                    .reduce("", (a,b) -> a.concat(",").concat(b)).concat("\n");
//            StringBuilder sb = new StringBuilder();
//            sb.append(moves.get(0).getName());
//            for (int i = 1; i < moves.size(); ++i) {
//                sb.append(",").append(moves.get(i).getName());
//            }
//            sb.append("\n");
//            return sb.toString();
        }
        return "";
    }

    @Override
    public int compareTo(Cycle o) {
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

    public static List<Cycle> condense(List<Cycle> input) {
        Set<Cycle> cycleSet = new HashSet<>();
        for (Cycle cycle : input) {
            cycleSet.addAll(cycle.splitIntoAllSubCycles());
        }
        return cycleSet.stream().toList();
    }
}
