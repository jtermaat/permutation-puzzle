package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Builder
@Data
public class Move {
    final private Integer id;
    final private String name;
    final private int[] newPositions;
    private List<int[]> swaps;
    final private boolean isInversion;
    private Move inverse;
    private Integer targetMatchCount;

    public Move(int id, String name, int[] newPositions, boolean inverted) {
        this.name = name;
        if (!inverted) {
            this.newPositions = newPositions;
            isInversion = false;
        } else {
            this.newPositions = new int[newPositions.length];
            List<Integer> newPositionsList = Arrays.stream(newPositions)
                    .boxed()
                    .toList();
            for (int i = 0;i<newPositions.length;++i) {
                this.newPositions[i] = newPositionsList.indexOf(i);
            }
            isInversion = true;
        }
        this.id = id;
        initSwaps();
    }

    public void initSwaps() {
        Queue<List<Integer>> swapsQueue = new PriorityQueue<>((a, b) -> a.get(1).compareTo(b.get(1)));
        for (int i = 0;i<newPositions.length;i++) {
            if (newPositions[i] != i) {
                List<Integer> wrapperSwap = new ArrayList<>(2);
                wrapperSwap.add(i);
                wrapperSwap.add(newPositions[i]);
                swapsQueue.offer(wrapperSwap);
            }
        }
        Map<Integer, List<Integer>> swapsMap = swapsQueue.stream()
                .collect(Collectors.toMap(a -> a.get(1), a -> a));
        swaps = new ArrayList<>();
        while(!swapsQueue.isEmpty()) {
            List<Integer> nextSwap = swapsQueue.poll();
            int swapIndex = nextSwap.get(0);
            int swapValue = nextSwap.get(1);
            boolean finished = false;
            while (swapIndex != swapValue && !finished) {
                swaps.add(new int[]{swapIndex, swapValue});
                nextSwap = swapsMap.get(swapIndex);
                if (!swapsQueue.contains(nextSwap)) {
                    finished = true;
                } else {
                    swapIndex = nextSwap.get(0);
                    swapValue = nextSwap.get(1);
                    swapsQueue.remove(nextSwap);
                }
            }
        }
    }

    public Move createInverted(int numIds) {
        return new Move(this.id+numIds, "-" + this.name, this.newPositions, true);
    }

    @Override
    public boolean equals(Object other) {
        return id.equals(((Move)other).getId());
    }

    @Override
    public String toString() {
        String returnString = ("Move " + name + ": ");
        for (int i = 0;i<newPositions.length;i++) {
            returnString = returnString + newPositions[i];
            if (i < newPositions.length - 1) {
                returnString = returnString + ",";
            }
        }
        return returnString;
    }
}
