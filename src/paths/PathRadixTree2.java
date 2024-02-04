package paths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PathRadixTree2 {

    private List<Short> positions;
//    private List<PathRadixTree2> nexts;
    private PathRadixTree2[] nexts;
    private List<Path> data;
    private int startIndex;


    public PathRadixTree2(short[] initPositions, Path path, int startIndex) {
        this.startIndex = startIndex;
        init(initPositions, path, startIndex);
    }

    public PathRadixTree2(short[] initPositions, Path path) {
        init(initPositions, path, 0);
    }

    public void init(short[] initPositions, Path path, int index) {
        positions = new ArrayList<>(initPositions.length - index);
        for (int i = index;i<initPositions.length;++i) {
            positions.add(initPositions[i]);
        }
        data = new ArrayList<>();
        data.add(path);
    }

    public List<Path> get(short[] checkPositions) {
        for (int i = 0;i<positions.size();++i) {
            if (checkPositions[i+startIndex] != positions.get(i)) {
                return null;
            }
        }
        if (startIndex + positions.size() == checkPositions.length) {
            return data;
        }
        if (nexts[checkPositions[startIndex + positions.size()]] == null) {
            return null;
        }
        return nexts[checkPositions[startIndex + positions.size()]].get(checkPositions);
    }

    public void put(short[] newPositions, Path path) {
        if (positions == null) {
            init(newPositions, path, 0);
        } else {
            for (int i = startIndex; i < startIndex + positions.size(); ++i) {
                if (newPositions[i] != positions.get(i - startIndex)) {
                    branch(i, newPositions, path);
                }
            }
            if (startIndex + positions.size() == newPositions.length) {
                data.add(path);
            } else {
                if (nexts[newPositions[startIndex + positions.size()]] == null) {
                    nexts[newPositions[startIndex + positions.size()]] =
                            new PathRadixTree2(newPositions, path, startIndex + positions.size());
                } else
                    nexts[newPositions[startIndex + positions.size()]].put(newPositions, path);
            }
        }
    }

    public void branch(int index, short[] newPositions, Path path) {
        List<Short> newMatchList = positions.subList(index-startIndex+1, positions.size());
        List<Short> myNewList = positions.subList(0, index-startIndex);
        PathRadixTree2[] newNext = new PathRadixTree2[newPositions.length];
        newNext[positions.get(index-startIndex)] = PathRadixTree2.builder()
                .positions(newMatchList)
                .data(data)
                .startIndex(index+1)
                .nexts(nexts)
                .build();
        newNext[newPositions[index]] = new PathRadixTree2(newPositions, path, index+1);
        this.data = null;
        this.positions = myNewList;
        this.nexts = newNext;
    }
}
