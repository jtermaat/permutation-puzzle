package paths;

import paths.MoveNode;
import lombok.Data;

@Data
public class Path {
    protected MoveNode start;
    protected MoveNode end;
    protected String name;

    @Override
    public String toString() {
        return name;
    }

    public Path(String name) {
        this.name = name;
    }

    public Path(MoveNode start, MoveNode end) {
        this.start = start;
        this.end = end;
    }
}
