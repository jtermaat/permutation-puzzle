package abstraction;

import lombok.Data;
import model.Move;

@Data
public class RelativeCubeMove implements Comparable<RelativeCubeMove> {
    private final int faceOffset;
    private final int numberShiftFromMiddle;
    private final boolean oppositeNumber;
    private final int inversionNumber;
    private final Move[][][] allowedMoves;

    public final static int NON_INVERTED = 0;
    public final static int INVERTED = 1;

    public static Move[][][] getStructuredMoveList(Move[] allowedMoves) {
        int maxNumber = 0;
        int maxFace = 0;
        for (Move move : allowedMoves) {
            maxNumber = Math.max(maxNumber, move.getNumber());
            maxFace = Math.max(maxFace, move.getFace());
        }
        Move[][][] returnVal = new Move[maxFace+1][maxNumber+1][2];
        for (Move move : allowedMoves) {
            returnVal[move.getFace()][move.getNumber()][move.getInversionNumber()] = move;
        }
        return returnVal;
    }

    public RelativeCubeMove(Move[][][] allowedMoves, Move move1, Move move2) {
        this.allowedMoves = allowedMoves;
        this.inversionNumber = Math.floorMod((move2.getInversionNumber() + move1.getInversionNumber()), 2);
        this.faceOffset = Math.floorMod((move2.getFace() - (move1.getFace())), allowedMoves.length);
        double middle = ((double)allowedMoves[0].length-1.0) / 2.0;
        double startDistFromMiddle = (double)move1.getNumber() - middle;
        double endDistFromMiddle = (double)move2.getNumber() - middle;
        if (endDistFromMiddle * startDistFromMiddle == 0) {
            this.oppositeNumber = endDistFromMiddle - startDistFromMiddle < 0;
        } else if (endDistFromMiddle * startDistFromMiddle < 0) {
            this.oppositeNumber = true;
        } else {
            this.oppositeNumber = false;
        }
        this.numberShiftFromMiddle = (int)(Math.abs(endDistFromMiddle) - Math.abs(startDistFromMiddle));
    }

    public RelativeCubeMove(int faceOffset, int numberShift, boolean oppositeNumber, int inversionNumber, Move[][][] allowedMoves) {
        this.faceOffset = faceOffset;
        this.numberShiftFromMiddle = numberShift;
        this.oppositeNumber = oppositeNumber;
        this.allowedMoves = allowedMoves;
        this.inversionNumber = inversionNumber;
    }

    public RelativeCubeMove(String str, Move[][][] allowedMoves) {
        String[] numbers = str.replaceAll("\\(", "")
                .replaceAll("\\)", "").trim().split("\\.");
        this.faceOffset = Integer.parseInt(numbers[0]);
        this.numberShiftFromMiddle = Integer.parseInt(numbers[1]);
        this.oppositeNumber = Integer.parseInt(numbers[2]) == 1;
        this.inversionNumber = Integer.parseInt(numbers[3]);
        this.allowedMoves = allowedMoves;
    }

    public Move getRelativeTo(Move move, boolean forward) {
        int inversionOffset = Math.floorMod((move.getInversionNumber() + inversionNumber), 2);
        int effectiveFaceOffset = faceOffset;
        int shift = numberShiftFromMiddle;

        boolean effectiveOppositeNumber = oppositeNumber;
        if (!forward) {
            effectiveFaceOffset = effectiveFaceOffset * -1;
            shift = shift * -1;
        }
        return allowedMoves
                [Math.floorMod(move.getFace() + effectiveFaceOffset, allowedMoves.length)]
                [getRelativeNumber(move.getNumber(), effectiveOppositeNumber, shift)]
                [inversionOffset];
    }

    protected int getRelativeNumber(int number, boolean opposite, int shift) {
        double middle = (((double) allowedMoves[0].length) - 1.0) / 2.0;
        double dist = number - middle;
        double newDist = Math.abs(dist) + shift;
        if (dist < 0) {
            opposite = !opposite;
        }
        final int subtractionNumber = 0;
        int newNumber = (int)(middle + newDist);
        if (opposite) {
            return Math.floorMod(allowedMoves[0].length - newNumber - 1, allowedMoves[0].length);
        } else {
            return Math.floorMod(newNumber, allowedMoves[0].length);
        }
    }

    public boolean matches(Move move1, Move move2, boolean forward) {
        return this.getRelativeTo(move1, forward).equals(move2);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        RelativeCubeMove otherMove = (RelativeCubeMove)o;
        if (faceOffset != otherMove.getFaceOffset()) {
            return false;
        }
        if (numberShiftFromMiddle != otherMove.getNumberShiftFromMiddle()) {
            return false;
        }
        if (oppositeNumber != otherMove.isOppositeNumber()) {
            return false;
        }
        if (inversionNumber != otherMove.getInversionNumber()) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RelativeCubeMove o) {
        if (faceOffset == o.getFaceOffset()) {
            if (numberShiftFromMiddle == o.getNumberShiftFromMiddle()) {
                if (oppositeNumber == o.isOppositeNumber()) {
                    return Integer.compare(inversionNumber, o.getInversionNumber());
                } else {
                    return Boolean.compare(oppositeNumber, o.isOppositeNumber());
                }
            } else {
                return Integer.compare(numberShiftFromMiddle, o.getNumberShiftFromMiddle());
            }
        } else {
            return Integer.compare(faceOffset, o.getFaceOffset());
        }
    }

    @Override
    public String toString() {
        int isOppositeInt = oppositeNumber ? 1 : 0;
        return "(" + faceOffset + "." + numberShiftFromMiddle + "." + isOppositeInt + "." + inversionNumber + ")";
    }
}
