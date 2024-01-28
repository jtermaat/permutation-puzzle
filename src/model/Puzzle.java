package model;

import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@Builder
public class Puzzle {
    private final int id;
    private final String puzzleType;
    private PuzzleInfo puzzleInfo;
    private final short[] initialState;
    private final boolean[][] solutionState;
    private final int numWildcards;
    private List<Path> paths;
    private MoveNode solution;

    public int getSolutionLength() {
        return solution.getLength();
    }

    public static Puzzle getFromString(String str) {
        try {
            final String[] parts = str.split(",");
            final int newId = Integer.parseInt(parts[0]);
            final String newType = parts[1];
            final int newNumWildcards = Integer.parseInt(parts[4]);
            final String[] newSolutionStateStrings = parts[2].split(";");
            final String[] newInitialStateStrings = parts[3].split(";");
            final short[] newInitialState = new short[newInitialStateStrings.length];
            final Map<String, Set<Short>> stringToIntSetMap = new HashMap<>();
            for (short i = 0; i < newInitialState.length; ++i) {
                newInitialState[i] = i;
                stringToIntSetMap.putIfAbsent(newInitialStateStrings[i], new HashSet<>());
                stringToIntSetMap.get(newInitialStateStrings[i]).add(i);
            }
            final boolean[][] newSolutionState = new boolean[newSolutionStateStrings.length][newSolutionStateStrings.length];
            for (int i = 0;i < newSolutionState.length;i++) {
                for (int j = 0;j<newSolutionState[i].length;j++) {
                    newSolutionState[i][j] = stringToIntSetMap.get(newSolutionStateStrings[i]).contains(j);
                }
            }

            return Puzzle.builder()
                    .id(newId)
                    .puzzleType(newType)
                    .numWildcards(newNumWildcards)
                    .initialState(newInitialState)
                    .solutionState(newSolutionState)
                    .build();
        } catch (NumberFormatException e) {
            System.out.println("Skipping header: " + str);
        }
        return null;
    }

    public static List<Puzzle> readPuzzleList(String filename, String solutionFilename, PuzzleInfo puzzleInfo) {
        try {
            String str = Files.readString(new File(filename).toPath(), Charset.defaultCharset());
            String solutionStr = Files.readString(new File(solutionFilename).toPath(), Charset.defaultCharset());
            String[] solutionParts = solutionStr.split("\n");
            Map<Integer, List<Move>> idToMoveListMap = new HashMap<>();
            for (int i = 0;i<solutionParts.length;++i) {
                try {
                    String[] solutionSubParts = solutionParts[i].split(",");
                    int thisId = Integer.parseInt(solutionSubParts[0]);
                    List<Move> moves = getMovesFromString(solutionSubParts[1], puzzleInfo);
//                    System.out.println("Adding moves " + moves + " from string " + solutionSubParts[1] + " to id " + thisId);
                    idToMoveListMap.put(thisId, moves);
                } catch (NumberFormatException e) {
                    System.out.println("Skipping line " + solutionParts[i]);
                }
            }
            List<Puzzle> puzzleList = Arrays.stream(str.split("\n")).map(Puzzle::getFromString)
                    .filter(Objects::nonNull)
                    .toList();
            puzzleList.forEach(p -> p.setSolution(MoveNode.fromList(idToMoveListMap.get(p.getId()))));
            puzzleList.forEach(p -> p.setPuzzleInfo(puzzleInfo));
            return puzzleList;

        } catch (IOException ie) {
            System.out.println("Error reading puzzle objects from file " + filename);
            ie.printStackTrace();
            return null;
        }
    }

    public static List<Move> getMovesFromString(String str, PuzzleInfo puzzleInfo) {
        List<Move> moves = new ArrayList<>();
        Map<String, Move> moveMap = List.of(puzzleInfo.getAllowedMoves()).stream()
                .collect(Collectors.toMap(Move::getName, Function.identity()));
        String[] parts = str.split("\\.");
        for (int i = 0;i<parts.length;++i) {
            moves.add(moveMap.get(parts[i]));
        }
        return moves;
    }
}
