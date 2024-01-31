package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Builder
@Data
public class Move implements Comparable<Move> {
    final private Integer id;
    final private String name;
    final private short[] newPositions;
    short[][] swaps;
    final private boolean isInversion;
    private Move inverse;
    private Integer targetMatchCount;

    private int face;
    private int number;

    public Move(int id, String name, short[] newPositions, boolean inverted, int face, int number) {
        this(id, name, newPositions, inverted);
        this.face = face;
        this.number = number;
    }

    public Move(int id, String name, short[] newPositions, boolean inverted) {
        this.name = name;
        if (!inverted) {
            this.newPositions = newPositions;
            isInversion = false;
            this.id = id;
        } else {
            this.newPositions = new short[newPositions.length];
            this.id = id * -1;
            List<Short> newPositionsList = new ArrayList<>(newPositions.length);
            for (short newPosition : newPositions) {
                newPositionsList.add(newPosition);
            }
            for (short i = 0;i<newPositions.length;++i) {
                this.newPositions[i] = (short)newPositionsList.indexOf(i);
            }
            isInversion = true;
        }
        initSwaps();
    }

    public void setFaceData(List<Character> allFaces) {
        this.face = allFaces.indexOf(name.charAt(0));

        this.number = name.length() == 1 ? 0 : Integer.parseInt(name.substring(1));
//        System.out.println("Face for " + name + " is " + this.face);
//        System.out.println("Number for " + name + " is " + this.number);
    }

    public void initSwaps() {
        Queue<List<Short>> swapsQueue = new PriorityQueue<>((a, b) -> a.get(1).compareTo(b.get(1)));
        for (short i = 0;i<newPositions.length;i++) {
            if (newPositions[i] != i) {
                List<Short> wrapperSwap = new ArrayList<>(2);
                wrapperSwap.add(i);
                wrapperSwap.add(newPositions[i]);
                swapsQueue.offer(wrapperSwap);
            }
        }
        Map<Short, List<Short>> swapsMap = swapsQueue.stream()
                .collect(Collectors.toMap(a -> a.get(1), a -> a));
        List<short[]> swapsList = new ArrayList<>();
        while(!swapsQueue.isEmpty()) {
            List<Short> nextSwap = swapsQueue.poll();
            short swapIndex = nextSwap.get(0);
            short swapValue = nextSwap.get(1);
            boolean finished = false;
            while (swapIndex != swapValue && !finished) {
                swapsList.add(new short[]{swapIndex, swapValue});
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
        Object[] swapsObject = swapsList.toArray();
        swaps = new short[swapsObject.length][2];
        for (int i = 0;i<swapsObject.length;i++) {
            swaps[i] = (short[])swapsObject[i];
        }
    }

    public Move createInverted(int numIds) {
        return new Move(this.id+numIds, "-" + this.name, this.newPositions, true, this.face, this.number);
    }

    @Override
    public boolean equals(Object other) {
        return other != null && getClass() == other.getClass() && id.equals(((Move)other).getId());
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public String toString() {
//        String returnString = ("Move " + name + ": ");
//        for (int i = 0;i<newPositions.length;i++) {
//            returnString = returnString + newPositions[i];
//            if (i < newPositions.length - 1) {
//                returnString = returnString + ",";
//            }
//        }
//        return returnString;
        return this.getName();
    }

    @Override
    public int compareTo(Move o) {
        if (o.isInversion() && !this.isInversion) {
            return -1;
        } else if (!o.isInversion && this.isInversion) {
            return 1;
        } else {
            if (this.face == o.getFace()) {
                return Integer.compare(this.number, o.getNumber());
            } else {
                return Integer.compare(this.face, o.getFace());
            }
        }
    }

    public int getInversionNumber() {
        if (isInversion) {
            return RelativeCubeMove.INVERTED;
        } else {
            return RelativeCubeMove.NON_INVERTED;
        }
    }
}
