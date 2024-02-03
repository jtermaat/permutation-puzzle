package paths;

import lombok.Data;

@Data
public class Path {
    protected MoveNode start;
    protected MoveNode end;

    public Path(MoveNode start, MoveNode end) {
        this.start = start;
        this.end = end;
    }
}
