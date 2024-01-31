package model;

import lombok.Data;

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
        System.out.println("Making relative move from " + move1 + " and " + move2);
        double middle = ((double)allowedMoves[0].length-1.0) / 2.0;
        System.out.println("Middle: " + middle);
        double startDistFromMiddle = (double)move1.getNumber() - middle;
        System.out.println("start dist from middle: " + startDistFromMiddle);
        double endDistFromMiddle = (double)move2.getNumber() - middle;
        System.out.println("end dist from middle: " + endDistFromMiddle);
        if (endDistFromMiddle * startDistFromMiddle == 0) {
            this.oppositeNumber = endDistFromMiddle - startDistFromMiddle < 0;
            System.out.println("Setting opposite number to " + (endDistFromMiddle - startDistFromMiddle < 0));
        } else if (endDistFromMiddle * startDistFromMiddle < 0) {
            this.oppositeNumber = true;
            System.out.println("Setting opposite number to true.");
        } else {
            this.oppositeNumber = false;
            System.out.println("Setting opposite number to false");
        }
//        System.out.println("New shift: " + (int)(Math.abs(endDistFromMiddle) - Math.abs(startDistFromMiddle)));
//        this.numberShiftFromMiddle = (int)(Math.abs(endDistFromMiddle) - Math.abs(startDistFromMiddle));


        System.out.println("New shift: " + (int)(Math.abs(endDistFromMiddle) - Math.abs(startDistFromMiddle)));
        this.numberShiftFromMiddle = (int)(Math.abs(endDistFromMiddle) - Math.abs(startDistFromMiddle));
    }

    public RelativeCubeMove(int faceOffset, int numberShift, boolean oppositeNumber, int inversionNumber, Move[][][] allowedMoves) {
        this.faceOffset = faceOffset;
        this.numberShiftFromMiddle = numberShift;
        this.oppositeNumber = oppositeNumber;
        this.allowedMoves = allowedMoves;
        this.inversionNumber = inversionNumber;
    }

    public Move getRelativeTo(Move move, boolean forward) {
//        if (this.toString().equals("(1.0.0.0)")) {
//            System.out.println("Getting " + move + " relative to (1.0.0.0).");
//        }
        int inversionOffset = Math.floorMod((move.getInversionNumber() + inversionNumber), 2);
//        if (!forward) {
//            inversionOffset = Math.floorMod((inversionOffset + 1), 2);
//        }
//        boolean effectiveOpposite = oppositeNumber;
//        if (inversionOffset > 0) {
//            effectiveOpposite = !effectiveOpposite;
//        }
//        if (this.toString().equals("(1.0.0.0)")) {
//            System.out.println("RETURNING " + allowedMoves[Math.floorMod((move.getFace() - (faceOffset)), allowedMoves.length)]
////                    [getRelativeNumber(move.getNumber(), inversionOffset)]
//                    [getRelativeNumber(move.getNumber(), effectiveOpposite)]
//                    [inversionOffset]);
//        }
        int effectiveFaceOffset = faceOffset;
//        if (inversionOffset > 0) {
//            effectiveFaceOffset = effectiveFaceOffset * -1;
//        }



//        if (!forward) {
//            inversionOffset = Math.floorMod(inversionOffset + 1, 2);
//        }
        int shift = numberShiftFromMiddle;

        boolean effectiveOppositeNumber = oppositeNumber;
        if (!forward) {
            effectiveFaceOffset = effectiveFaceOffset * -1;
            shift = shift * -1;
//            effectiveOppositeNumber = !effectiveOppositeNumber;
//            effectiveFaceOffset = effectiveFaceOffset + 1;
        }
//        if (inversionOffset > 0) {
//            effectiveFaceOffset *= -1;
//        }
//        if (oppositeNumber) {
//            inversionOffset = Math.floorMod(inversionOffset + 1, 2);
////            effectiveFaceOffset = effectiveFaceOffset * -1;
//        }
//        if (!forward) {
//            effectiveOppositeNumber = !oppositeNumber;
//        }
//        if (inversionOffset > 0) {
//            effectiveOppositeNumber = !oppositeNumber;
//        }
//        if (!forward) {
//            effectiveOppositeNumber = !oppositeNumber;
//        }
//        if (!forward) {
//            inversionOffset = Math.floorMod(inversionOffset + 1, 2);
//        }

//        int returnFace = forward ? Math.floorMod(move.getFace() + effectiveFaceOffset, allowedMoves.length)
//                : Math.floorMod(move.getFace() + effectiveFaceOffset, allowedMoves.length);

        return allowedMoves
                [Math.floorMod(move.getFace() + effectiveFaceOffset, allowedMoves.length)]
//                [returnFace]
//                    [getRelativeNumber(move.getNumber(), inversionOffset)]
                [getRelativeNumber(move.getNumber(), effectiveOppositeNumber, shift)]
                [inversionOffset];
    }

//    private static boolean LOGGING_ON = true;
    private static boolean LOGGING_ON = false;

//    protected int getOppositeNumberValue(int number) {
//        double middle = ((double)allowedMoves[0].length -1.0) / 2.0;
//        double dist = (double)number - middle;
//        double newDist = number + dist;
//    }
    protected int getRelativeNumber(int number, boolean opposite, int shift) {
        if (LOGGING_ON) {
            System.out.println("Getting relative number for " + number + " with opposite " + opposite + " and shift " + shift);
        }
        double middle;
//        if (allowedMoves[0].length % 2 == 1) {
//            middle = (double)allowedMoves[0].length / 2.0;
//        }
//        else {
            middle = (((double) allowedMoves[0].length) - 1.0) / 2.0;
//        }
        if (LOGGING_ON) {
            System.out.println("Middle: " + middle);
        }
//        number = Math.floorMod(number, (int)middle);
        double dist = number - middle;
//        double dist = Math.abs(number - middle);
        double newDist = Math.abs(dist) + shift;
        if (LOGGING_ON) {
            System.out.println("dist: " + dist);
            System.out.println("shift: " + shift);
            System.out.println("new dist : " + newDist);
        }
        if (dist < 0) {
            opposite = !opposite;
        }

        final int subtractionNumber = 0;
        // final int subtractionNumber = 1;

        int newNumber = (int)(middle + newDist);
//        System.out.println("New number: " + newNumber);
        if (opposite) {
            if (LOGGING_ON) {
                System.out.println("Returning " + Math.floorMod(allowedMoves[0].length - newNumber - 1, allowedMoves[0].length));
            }
            return Math.floorMod(allowedMoves[0].length - newNumber - 1, allowedMoves[0].length);
//            System.out.println("returning " + -1 * newNumber);
//            return -1 * newNumber;
        } else {
            if (LOGGING_ON) {
                System.out.println("returning " + Math.floorMod(newNumber, allowedMoves[0].length));
            }
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

    protected int getComparisonScore() {
        int numberShift = numberShiftFromMiddle;
        if (oppositeNumber) {
            numberShift += (int)(allowedMoves[0].length / 2.0);
        }
        return inversionNumber + (2 * (numberShift + allowedMoves[0].length)) + (2 * allowedMoves[0].length * faceOffset);
    }

    @Override
    public String toString() {
        int isOppositeInt = oppositeNumber ? 1 : 0;
        return "(" + faceOffset + "." + numberShiftFromMiddle + "." + isOppositeInt + "." + inversionNumber + ")";
    }
}
