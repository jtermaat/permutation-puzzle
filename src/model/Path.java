package model;

import lombok.Data;
import traversal.Permutation;

@Data
public class Path extends Permutation {
    protected MoveNode start;
    protected MoveNode end;

    public Path(short[] positions, long gameHash) {
        this.gameHash = gameHash;
        this.positions = positions;
    }

    public Path(MoveNode start, MoveNode end, short[] positions) {
        super(positions);
        this.start = start;
        this.end = end;
    }

    public short[] getPositions() {
        return this.positions;
    }

}
