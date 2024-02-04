package traversal;

import lombok.Data;
import model.Move;

@Data
public class Mutator {

    protected short[] positions;


    public Mutator(int length) {
        this.positions = new short[length];
        resetPositions();
    }

    public Mutator(short[] positions) {
        this.positions = new short[positions.length];
        System.arraycopy(positions, 0, this.positions, 0, positions.length);
    }

    public void transform(final Move move) {
        short lastIndex = -1;
        short lastValue = -1;
        final short[][] swaps = move.getSwaps();
        for (short i = 0;i < swaps.length;++i) {
            if (lastIndex != swaps[i][1]) {
                lastIndex = swaps[i][0];
                lastValue = positions[swaps[i][0]];
                positions[swaps[i][0]] = positions[swaps[i][1]];
            } else {
                final short temp = positions[swaps[i][0]];
                positions[swaps[i][0]] = lastValue;
                lastIndex = swaps[i][0];
                lastValue = temp;
            }
        }
    }

    public void resetPositions() {
        for (short i = 0;i<positions.length;++i) {
            positions[i] = i;
        }
    }
}