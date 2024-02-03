package traversal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;

import java.util.List;
import java.util.stream.IntStream;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Mutator implements Comparable<Mutator> {

    protected short[] positions;
    protected long gameHash;
    private final static List<Long> powersOfTwo =  IntStream.range(0, 10000).boxed()
            .map(i -> (long) Math.pow(2, i))
            .toList();


    public Mutator(int length) {
        this.positions = new short[length];
        resetPositions();
    }

    public Mutator(short[] positions) {
        this.positions = new short[positions.length];
        System.arraycopy(positions, 0, this.positions, 0, positions.length);
        this.calculateGameHash();
    }

    protected void calculateGameHash() {
        gameHash = 0L;
        for (int i = 0;i<positions.length;++i) {
            gameHash += powersOfTwo.get(i) * positions[i];
        }
    }

    public void transform(final Move move) {
        short lastIndex = -1;
        short lastValue = -1;
        final short[][] swaps = move.getSwaps();
        for (short i = 0;i < swaps.length;++i) {
            if (lastIndex != swaps[i][1]) {
                lastIndex = swaps[i][0];
                lastValue = positions[swaps[i][0]];
                this.gameHash += powersOfTwo.get(swaps[i][0]) * positions[swaps[i][1]]
                        - powersOfTwo.get(swaps[i][0]) * positions[swaps[i][0]];
                positions[swaps[i][0]] = positions[swaps[i][1]];
            } else {
                final short temp = positions[swaps[i][0]];
                this.gameHash += powersOfTwo.get(swaps[i][0]) * lastValue
                        - powersOfTwo.get(swaps[i][0]) * positions[swaps[i][0]];
                positions[swaps[i][0]] = lastValue;
                lastIndex = swaps[i][0];
                lastValue = temp;
            }
        }
    }

    public void resetPositions() {
        for (short i = 0;i<positions.length;++i) {
            positions[i] = i;
            calculateGameHash();
        }
    }

    @Override
    public int compareTo(Mutator otherMutator) {
        for (short i = 0;i<positions.length;++i) {
            if (positions[i] < otherMutator.getPositions()[i]) {
                return -1;
            } else if (positions[i] > otherMutator.getPositions()[i]) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !getClass().equals(o.getClass())) {
            return false;
        }
        Mutator otherMutator = (Mutator) o;
        for (short i = 0;i<positions.length;++i) {
            if (positions[i] != otherMutator.getPositions()[i]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return (int)gameHash;
    }

}
