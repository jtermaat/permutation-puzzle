package traversal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.Move;
import model.Path;

@AllArgsConstructor
@Data
public class Permutation implements Comparable<Permutation> {

    protected int[] positions;
    protected long gameHash;
    final private long[] powersOfTwo;


    public Permutation(int[] positions) {
        this.positions = new int[positions.length];
        System.arraycopy(positions, 0, this.positions, 0, positions.length);
        powersOfTwo = new long[positions.length];
        for (int i = 0;i<powersOfTwo.length;++i) {
            powersOfTwo[i] = (long)Math.pow(2, i);
        }
        this.calculateGameHash();
    }

    protected void calculateGameHash() {
        gameHash = 0L;
        for (int i = 0;i<positions.length;++i) {
            gameHash += powersOfTwo[i] * positions[i];
        }
    }

    protected void recordCountChanges(int index, int newValue) {
        this.gameHash += powersOfTwo[index] * newValue - powersOfTwo[index] * positions[index];
    }

    public void transform(final Move move) {
        int lastIndex = -1;
        int lastValue = -1;
        final int[][] swaps = move.getSwaps();
        for (int i = 0;i < swaps.length;++i) {
            if (lastIndex != swaps[i][1]) {
                lastIndex = swaps[i][0];
                lastValue = positions[swaps[i][0]];
                recordCountChanges(swaps[i][0], positions[swaps[i][1]]);
                positions[swaps[i][0]] = positions[swaps[i][1]];
            } else {
                final int temp = positions[swaps[i][0]];
                recordCountChanges(swaps[i][0], lastValue);
                positions[swaps[i][0]] = lastValue;
                lastIndex = swaps[i][0];
                lastValue = temp;
            }
        }
    }

    @Override
    public int compareTo(Permutation otherPermutation) {
        for (int i = 0;i<positions.length;++i) {
            if (positions[i] < otherPermutation.getPositions()[i]) {
                return -1;
            } else if (positions[i] > otherPermutation.getPositions()[i]) {
                return 1;
            }
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Permutation otherPermutation = (Permutation) o;
        for (int i = 0;i<positions.length;++i) {
            if (positions[i] != otherPermutation.getPositions()[i]) {
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
