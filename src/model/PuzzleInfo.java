package model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class PuzzleInfo {
    private String puzzleType;

    private List<Move> allowedMoves;

    public void getFromString(String puzzleTypeString, String allowedMovesString) throws JsonProcessingException {
        ObjectMapper obj = new ObjectMapper();
        Map<String, List<Integer>> movesMap = (Map<String, List<Integer>>)obj.readValue(allowedMovesString, Map.class);
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
        List<Move> inverses = new ArrayList<>();
        for (Move move : allowed) {
            Move inverse = move.getInverse();
            inverse.setInverse(move);
            move.setInverse(inverse);
            inverses.add(inverse);
        }
        allowedMoves = new ArrayList<>();
        allowedMoves.addAll(allowed);
        allowedMoves.addAll(inverses);

    }

    public static void main(String[] args) {

    }
}
