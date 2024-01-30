package main;

import model.PuzzleInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SubmissionCombiner {
    final static String IMPROVED_FILE = "/Users/johntermaat/Downloads/submission-globe_6-8.csv";
    final static String MAIN_FILE = "/Users/johntermaat/Downloads/submission.csv";
    final static String OUTPUT_FILE = "/Users/johntermaat/Downloads/submission.csv";

    public static void main(String[] args) {
        try {
            String[] mainLines = Files.readString(new File(MAIN_FILE).toPath(), Charset.defaultCharset()).split("\n");
            String[] improvedLines = Files.readString(new File(IMPROVED_FILE).toPath(), Charset.defaultCharset()).split("\n");
            Map<Integer, String> improvedLinesMap = Arrays.stream(improvedLines)
                    .collect(Collectors.toMap(line -> {
                        return Integer.parseInt(line.split(",")[0]);
                    }, Function.identity()));
            BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE));
            for (String mainLine : mainLines) {
                try {
                    Integer lineId = Integer.parseInt(mainLine.split(",")[0]);
                    writer.write(improvedLinesMap.getOrDefault(lineId, mainLine));
                } catch (Exception e) {
                    writer.write(mainLine);
                } finally {
                    writer.write("\n");
                }
            }
            writer.close();
        } catch (IOException ie) {
            System.out.println("Error combining files " + MAIN_FILE + " and " + IMPROVED_FILE + " or error writing " + OUTPUT_FILE);
            ie.printStackTrace();
        }
    }
}
