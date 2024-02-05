package model;

import abstraction.Cycle;
import paths.MoveNode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Solution {
    private Map<Integer, MoveNode> idToMovesMap;
    private Map<String, List<MoveNode>> puzzleTypeToMovesMap;

    public Solution(String solutionFile, Map<String, PuzzleInfo> puzzleInfoMap, List<Puzzle> puzzleList) {
        idToMovesMap = new HashMap<>();
        puzzleTypeToMovesMap = new HashMap<>();
        Map<Integer, Puzzle> puzzleMap = puzzleList.stream()
                .collect(Collectors.toMap(Puzzle::getId, Function.identity()));
        String solutionStr = Puzzle.readFileString(solutionFile);
        String[] solutionParts = solutionStr.split("\n");
        for (String solutionPart : solutionParts) {
            try {
                String[] solutionSubParts = solutionPart.split(",");
                int thisId = Integer.parseInt(solutionSubParts[0]);
                PuzzleInfo puzzleInfo = puzzleInfoMap.get(puzzleMap.get(thisId).getPuzzleType());
                List<Move> moves = getMovesFromString(solutionSubParts[1], puzzleInfo);
                MoveNode solutionNode = new MoveNode(moves);
                idToMovesMap.put(thisId, solutionNode);
                puzzleTypeToMovesMap.putIfAbsent(puzzleInfo.getPuzzleType(), new ArrayList<>());
                puzzleTypeToMovesMap.get(puzzleInfo.getPuzzleType()).add(solutionNode);
                puzzleMap.get(thisId).setSolution(solutionNode);
            } catch (NumberFormatException e) {
                System.out.println("Skipping line " + solutionPart);
            }
        }
    }

    public int totalMoveCount() {
        return (int)idToMovesMap.keySet().stream()
                .mapToLong(k -> idToMovesMap.get(k).tolist().size())
                .sum();
    }

    public int moveCountForType(String type) {
        return (int)puzzleTypeToMovesMap.get(type).stream()
                .mapToLong(j -> j.tolist().size())
                .sum();
    }

    public void writeToFile(String filename) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            for (Integer id : idToMovesMap.keySet()) {
                List<Move> allMoves = idToMovesMap.get(id).tolist();
                if (!allMoves.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(id).append(",");
                    sb.append(allMoves.get(0).getName());
                    for (int j = 1; j < allMoves.size(); ++j) {
                        sb.append(".").append(allMoves.get(j).getName());
                    }
                    sb.append("\n");
                    writer.write(sb.toString());
                }
            }
            writer.close();
        } catch (IOException ie) {
            System.out.println("Error writing results to file " + filename);
            ie.printStackTrace();
        }
    }

    public void optimizeRoutes() {
        idToMovesMap.keySet().forEach(k -> idToMovesMap.get(k).optimizeRoute());
    }

    private static List<Move> getMovesFromString(String str, PuzzleInfo puzzleInfo) {
        try {
            List<Move> moves = new ArrayList<>();
            Map<String, Move> moveMap = Arrays.stream(puzzleInfo.getAllowedMoves())
                    .collect(Collectors.toMap(Move::getName, Function.identity()));
            String[] parts = str.split("\\.");
            for (int i = 0; i < parts.length; ++i) {
                moves.add(moveMap.get(parts[i]));
            }
            if (puzzleInfo.getPuzzleType().toUpperCase().contains("CUBE")) {
                moves = Cycle.cubeSortMoves(moves);
            }
            return moves;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
