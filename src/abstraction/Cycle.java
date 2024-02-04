package abstraction;

import lombok.Builder;
import lombok.Data;
import model.Move;
import paths.Shortcut;
import traversal.Mutator;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            Mutator testMutator = new Mutator(moves.get(0).getNewPositions().length);
            for (int i = 0; i < moves.size(); ++i) {
                testMutator.resetPositions();
                for (int j = i; j < moves.size(); ++j) {
                    testMutator.transform(moves.get(j));
                    boolean matches = true;
                    for (int k = 0; k< testMutator.getPositions().length; ++k) {
                        matches = matches && testMutator.getPositions()[k] == k;
                    }
                    if (matches && !(i == 0 && j+1 == moves.size())) {
                        return split(i, j+1);
                    }
                }
            }
        }
        return Collections.singletonList(this);
    }

    public List<Cycle> split(int startIndex, int endIndex) {
        List<Move> moveList1 = moves.subList(startIndex, endIndex);
        List<Move> moveList2 = new ArrayList<>(moves.size() - moveList1.size());
        moveList2.addAll(moves.subList(endIndex, moves.size()));
        moveList2.addAll(moves.subList(0, startIndex));
        Cycle newCycle1 = new Cycle(moveList1);
        newCycle1.cubeSortMoves();
        Cycle newCycle2 = new Cycle(moveList2);
        newCycle2.cubeSortMoves();
        return Arrays.asList(newCycle1, newCycle2);
    }

    public Cycle getInverse() {
        return Cycle.builder()
                .moves(moves.stream()
                        .map(Move::getInverse)
                        .toList())
                .build();
    }

    public boolean isValid() {
        return validateEquality(moves, Collections.emptyList());
    }

    public static List<Move> cubeSortMoves(List<Move> moves) {
        if (moves.size() > 1) {
            int currentFace = -1;
            List<List<Move>> groupedList = new ArrayList<>();
            List<Move> currentList = new ArrayList<>();
            for (int i = 0;i<moves.size();++i) {
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
        }
        return moves;
    }

    public void cubeSortMoves() {
        if (moves.size() > 1) {
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

    public static boolean validateEquality(List<Move> list1, List<Move> list2) {
        if (list1.isEmpty() && list2.isEmpty()) {
            return true;
        }
        int length = list1.isEmpty() ? list2.getFirst().getNewPositions().length : list1.getFirst().getNewPositions().length;
        short[] startPositions1 = new short[length];
        short[] startPositions2 = new short[length];
        for (short i = 0;i<startPositions1.length;++i) {
            startPositions1[i] = i;
            startPositions2[i] = i;
        }

        final Mutator checker1 = new Mutator(startPositions1);
        Mutator checker2 = new Mutator(startPositions2);
        list1.forEach(checker1::transform);
        list2.forEach(checker2::transform);

        boolean matches = true;
        for (int i = 0;i<checker1.getPositions().length;++i) {
            if (checker1.getPositions()[i] != checker2.getPositions()[i]) {
                matches = false;
            }
        }
        return matches;
    }
}
