package model;

import lombok.Data;

import java.util.List;

@Data
public class RelativeMove implements Comparable<RelativeMove> {
    private final int faceOffset;
    private final int numberOffset;
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

    public RelativeMove(Move[][][] allowedMoves, Move move1, Move move2) {
        this.allowedMoves = allowedMoves;
        this.faceOffset = Math.floorMod((move2.getFace() - move1.getFace()), allowedMoves.length);
        this.numberOffset = move2.getNumber() - move1.getNumber();
        this.inversionNumber = Math.floorMod((move2.getInversionNumber() + move1.getInversionNumber()), 2);
    }

    public RelativeMove(int faceOffset, int numberOffset, int inversionNumber, Move[][][] allowedMoves) {
        this.faceOffset = faceOffset;
        this.numberOffset = numberOffset;
        this.allowedMoves = allowedMoves;
        this.inversionNumber = inversionNumber;
    }

    public RelativeMove(int faceOffset, int numberOffset, boolean inverted, Move[][][] allowedMoves) {
        this(faceOffset, numberOffset, (inverted ? 1 : 0), allowedMoves);
    }

    public Move getRelativeTo(Move move) {
        try {
            return allowedMoves
                    [Math.floorMod((move.getFace() + faceOffset), allowedMoves.length)]
                    [move.getNumber() + numberOffset]
                    [Math.floorMod((move.getInversionNumber() + inversionNumber), 2)];
        } catch (ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        RelativeMove otherMove = (RelativeMove)o;
        if (faceOffset != otherMove.getFaceOffset()) {
            return false;
        }
        if (numberOffset != otherMove.getNumberOffset()) {
            return false;
        }
        if (inversionNumber != otherMove.getInversionNumber()) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(RelativeMove o) {
        if (faceOffset == o.getFaceOffset()) {
            if (numberOffset == o.getNumberOffset()) {
                return Integer.compare(inversionNumber, o.getInversionNumber());
            } else {
                return Integer.compare(numberOffset, o.getNumberOffset());
            }
        } else {
            return Integer.compare(faceOffset, o.getFaceOffset());
        }
    }

    protected int getComparisonScore() {
        return inversionNumber + (2 * (numberOffset + allowedMoves[0].length)) + (2 * allowedMoves[0].length * faceOffset);
    }

    @Override
    public String toString() {
        return "(" + faceOffset + "." + numberOffset + "." + inversionNumber + ")";
    }
}
