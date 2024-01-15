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
}

