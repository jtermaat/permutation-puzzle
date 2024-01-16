package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
public class PuzzleInfo {
    private String puzzleType;

    private List<Move> allowedMoves;

    public static PuzzleInfo getFromString(String lineString) {
        String[] parts = lineString.split(",");
        String puzzleType = parts[0];
        String allowedMovesString = parts[1];
        ObjectMapper obj = new ObjectMapper();
        try {
            Map<String, List<Integer>> movesMap = (Map<String, List<Integer>>) obj.readValue(allowedMovesString, Map.class);

            List<Move> allowed = new ArrayList<>();
            for (String a : movesMap.keySet()) {
                List<Integer> positions = movesMap.get(a);
                int[] newPositions = new int[positions.size()];
                for (int i = 0;i<positions.size();i++) {
                    newPositions[i] = positions.get(i);
                }
                allowed.add(Move.builder()
                        .name(a)
                        .newPositions(newPositions)
                        .build());
            }
            List<Move> allowedMoves = allowed.stream()
                    .flatMap(move -> {
                        Move inverse = move.getInverse();
                        inverse.setInverse(move);
                        move.setInverse(inverse);
                        return Stream.of(move, inverse);
                    }).toList();
        return PuzzleInfo.builder()
                .allowedMoves(allowedMoves)
                .puzzleType(puzzleType)
                .build();
        } catch(JsonProcessingException e) {
            System.out.println("Error reading Json: " + lineString);
            return null;
        }
    }

    public static List<PuzzleInfo> readPuzzleInfoList(String filename) {
        try {
            String str = Files.readString(new File(filename).toPath(), Charset.defaultCharset());
            return Arrays.stream(str.split("\n")).map(PuzzleInfo::getFromString)
                    .collect(Collectors.toList());
        } catch (IOException ie) {
            System.out.println("Error reading puzzle info objects from file " + filename);
            ie.printStackTrace();
            return null;
        }
    }
}
