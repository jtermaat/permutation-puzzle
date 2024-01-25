package model;

import lombok.Builder;
import lombok.Data;
import traversal.Permutation;

@Data
@Builder
public class Path extends Permutation {
    protected MoveNode start;
    protected MoveNode end;

    public Path(MoveNode start, MoveNode end, int[] positions) {
        super(positions);
        this.start = start;
        this.end = end;
    }

}
