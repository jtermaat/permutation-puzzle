package model;

import abstraction.Cycle;
import paths.MoveNode;
import lombok.Builder;
import lombok.Data;
import paths.Path;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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

    private static Puzzle getFromString(String str) {
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
            for (int i = 0; i < newSolutionState.length; i++) {
                for (int j = 0; j < newSolutionState[i].length; j++) {
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

    public static List<Puzzle> readPuzzleList(String filename) {
        String str = readFileString(new File(filename).toPath().toString());
        List<Puzzle> puzzleList = Arrays.stream(str.split("\n")).map(Puzzle::getFromString)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return puzzleList;
    }

    public static String readFileString(String filename) {
        try {
            return new String(Files.readAllBytes(Paths.get(filename)));
        } catch (IOException e) {
            System.err.println("Error reading file " + filename);
            e.printStackTrace();
            return "";
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }


}
