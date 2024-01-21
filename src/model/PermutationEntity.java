package model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PermutationEntity implements Comparable<PermutationEntity> {
    private final List<Move> moves;
    private final int[] positions;
    private final int matchesWithTargetCount;
    private final Integer changesCount;

    public Move toMove(int id) {
        Move move = Move.builder()
                .id(id)
                .name(moves.stream()
                        .map(Move::getName)
                        .reduce("", (a, b) -> a.concat(".").concat(b)))
                .isInversion(false)
                .newPositions(positions)
                .build();
        Move inverse = new Move(id * -1,
                moves.reversed().stream()
                .map(Move::getName)
                .reduce("", (a, b) -> a.concat(".").concat(b)),
                positions, true);

        inverse.initSwaps();
        move.initSwaps();
        move.setInverse(inverse);
        inverse.setInverse(move);
        return move;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Moves: ");
        for (int i = 0;i<moves.size();i++) {
            sb.append(moves.get(i).getName());
            if (i < moves.size()-1) {
                sb.append(".");
            }
        }
        sb.append("\n");
        sb.append("Positions: ");
        for (int i = 0;i<positions.length;i++) {
            sb.append(positions[i]);
            if (i < positions.length -1) {
                sb.append(",");
            }
        }
        sb.append("\nMatches count: " + matchesWithTargetCount);
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermutationEntity otherPermutation = (PermutationEntity) o;
        for (int i = 0;i<positions.length;++i) {
            if (positions[i] != otherPermutation.getPositions()[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int compareTo(PermutationEntity otherPermutation) {
        for (int i = 0;i<positions.length;++i) {
            if (positions[i] < otherPermutation.getPositions()[i]) {
                return -1;
            } else if (positions[i] > otherPermutation.getPositions()[i]) {
                return 1;
            }
        }
        return 0;
    }
}

