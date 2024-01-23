package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class PuzzleInfo {
    private String puzzleType;

    private List<Move> allowedMoves;

    public static PuzzleInfo getFromString(String lineString) {
        lineString = lineString.replaceAll("\n", "");
        try {
            String[] parts = lineString.split(",");
            String puzzleType = parts[0];
            String allowedMovesString = lineString.substring(parts[0].length()+2)
                    .replaceAll("'", "\"");
            ObjectMapper obj = new ObjectMapper();
            try {
                Map<String, List<Integer>> movesMap = (Map<String, List<Integer>>) obj.readValue(allowedMovesString, Map.class);

                final List<Move> allowed = new ArrayList<>();
                int moveIdCount = 0;
                for (String a : movesMap.keySet()) {
                    List<Integer> positions = movesMap.get(a);
                    int[] newPositions = new int[positions.size()];
                    for (int i = 0; i < positions.size(); i++) {
                        newPositions[i] = positions.get(i);
                    }
                    allowed.add(Move.builder()
                            .name(a)
                            .newPositions(newPositions)
                            .id(moveIdCount)
                            .build());
                    ++moveIdCount;
                }
                List<Move> allowedMoves = allowed.stream()
                        .flatMap(move -> {
                            Move inverse = move.createInverted(allowed.size());
                            inverse.setInverse(move);
                            move.setInverse(inverse);
                            return Stream.of(move, inverse);
                        }).toList();
                allowedMoves.forEach(Move::initSwaps);
                return PuzzleInfo.builder()
                        .allowedMoves(allowedMoves)
                        .puzzleType(puzzleType)
                        .build();
            } catch (JsonProcessingException e) {
                System.out.println("Error reading Json: " + lineString);
                return null;
            }
        } catch (Exception e) {
            System.out.println("Skipping header: " + lineString);
            e.printStackTrace();
        }
        return null;
    }

    public static List<PuzzleInfo> readPuzzleInfoList(String filename) {
        try {
            String str = Files.readString(new File(filename).toPath(), Charset.defaultCharset());
            return Arrays.stream(str.split("\n")).map(PuzzleInfo::getFromString)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (IOException ie) {
            System.out.println("Error reading puzzle info objects from file " + filename);
            ie.printStackTrace();
            return null;
        }
    }
}
