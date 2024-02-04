package paths;

import lombok.Data;

@Data
public class Path {
    protected MoveNode start;
    protected MoveNode end;
    protected int length;

    public Path(MoveNode start, MoveNode end, int length) {
        this.start = start;
        this.end = end;
        this.length = length;
    }
}
