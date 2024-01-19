package model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PermutationEntity {
    private final List<Move> moves;
    private final int[] positions;
    private final int matchesWithTargetCount;
    private final int changesCount;

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
}

