package main;

import lombok.Data;
import model.Puzzle;
import model.PuzzleInfo;
import model.Solution;
import paths.PathRadixTree;
import paths.Shortcut;
import traversal.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class PermutationDriver {
    private List<Puzzle> puzzles;
    private String puzzleType;
    private List<PuzzleInfo> puzzleInfos;
    private Map<String, PuzzleInfo> puzzleInfoMap;
    private Solution solution;
    private List<Shortcut> shortcuts;
    private boolean printShortcuts;
    private PathRadixTree radixTree;

    public PermutationDriver() {
        shortcuts = new ArrayList<>();
        radixTree = new PathRadixTree();
    }

    public static void main(String[] args) {
        PermutationDriver driver = new PermutationDriver();
        driver.loadPuzzleInfos("/Users/johntermaat/Downloads/puzzle_info.csv");
        driver.loadPuzzles("/Users/johntermaat/Downloads/puzzles.csv");
        driver.loadSolution("/Users/johntermaat/Downloads/submission3.csv");
        String puzzleType = "globe_3/33";
        driver.setPuzzleType(puzzleType);
        int searchDepth = 12;
        int maxLength = 100;
        int minLength = 3;
        driver.setPrintShortcuts(true);
        driver.collectPaths(maxLength, minLength);
        driver.randomSearch(searchDepth, 0, 1);
        System.out.println("Old solution length for " + puzzleType + ": " + driver.getSolution().moveCountForType(puzzleType));
        driver.activateShortcuts();
        System.out.println("New solution length for " + puzzleType + ": " + driver.getSolution().moveCountForType(puzzleType));
        driver.saveSolution("/Users/johntermaat/Downloads/submission4.csv");
    }

    public void collectPaths(int maxLength, int minLength) {
        System.out.println("Collecting paths for " + puzzleType + ".");
        long startTime = System.currentTimeMillis();
        int numPaths = puzzleInfoMap.get(puzzleType).getPuzzles().stream()
                .map(p -> new PathCollector(p.getSolution(), radixTree, maxLength, minLength))
                .map(PathCollector::collectPaths)
                .reduce(0, Integer::sum);
        long endTime = System.currentTimeMillis();
        double seconds = (double)(endTime - startTime) / (1000.0);
        System.out.println("Collected " + numPaths + " paths for " + puzzleType + " in " + seconds + " seconds.");
    }

    public void randomSearch(int maxDepth, int minDepth, int minutes) {
        RandomShortcutHunter hunter = new RandomShortcutHunter(puzzleInfoMap.get(puzzleType), maxDepth, radixTree, minutes);
        hunter.setMinDepth(minDepth);
        search(hunter);
    }

    public void bruteForceSearch(int maxDepth) {
        BruteForceShortcutHunter hunter = new BruteForceShortcutHunter(puzzleInfoMap.get(puzzleType), maxDepth, radixTree, printShortcuts);
        search(hunter);
    }

    public void activateShortcuts() {
        System.out.println("Activating shortcuts.");
        long startTime = System.currentTimeMillis();
        shortcuts.forEach(Shortcut::activate);
        System.out.println("Optimizing routes.");
        solution.optimizeRoutes();
        long endTime = System.currentTimeMillis();
        double seconds = (double)(endTime - startTime) / (1000.0);
        System.out.println("Activated shortcuts in " + seconds + " seconds.");
    }

    protected void search(ShortcutHunter hunter) {
        System.out.println("Starting shortcut search for " + puzzleType);
        hunter.setPrintShortcuts(this.printShortcuts);
        long startTime = System.currentTimeMillis();
        List<Shortcut> newShortcuts = hunter.performSearch();
        long endTime = System.currentTimeMillis();
        double processingMinutes = (double)(endTime - startTime) / (1000 * 60.0);
        System.out.println("Found " + newShortcuts.size() + " shortcuts for " + puzzleType + " in " + processingMinutes + " minutes.");
        shortcuts.addAll(newShortcuts);
    }

    public void loadPuzzleInfos(String filepath) {
        puzzleInfos = PuzzleInfo.readPuzzleInfoList(filepath);
        puzzleInfoMap = puzzleInfos.stream()
                .collect(Collectors.toMap(PuzzleInfo::getPuzzleType, Function.identity()));
        puzzleType = puzzleInfos.get(0).getPuzzleType();
        if (puzzles != null) {
            for (Puzzle puzzle : puzzles) {
                PuzzleInfo info = puzzleInfoMap.get(puzzle.getPuzzleType());
                puzzle.setPuzzleInfo(info);
                if (info.getPuzzles() == null) {
                    info.setPuzzles(new ArrayList<>());
                }
                info.getPuzzles().add(puzzle);
            }
        }
    }

    public void loadPuzzles(String filepath) {
        puzzles = Puzzle.readPuzzleList(filepath);
        if (puzzleInfoMap != null) {
            for (Puzzle puzzle : puzzles) {
                PuzzleInfo info = puzzleInfoMap.get(puzzle.getPuzzleType());
                puzzle.setPuzzleInfo(info);
                if (info.getPuzzles() == null) {
                    info.setPuzzles(new ArrayList<>());
                }
                info.getPuzzles().add(puzzle);
            }
        }
    }

    public void loadSolution(String filepath) {
        solution = new Solution(filepath, puzzleInfoMap, puzzles);
    }

    public void saveSolution(String filepath) {
        solution.writeToFile(filepath);
    }
}
