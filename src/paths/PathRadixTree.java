package paths;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
@AllArgsConstructor
public class PathRadixTree {

    private List<Short> positions;
    private PathRadixTree right;
    private PathRadixTree left;
    private PathRadixTree match;
    private List<Path> data;
    private int startIndex;

    public PathRadixTree(short[] initPositions, Path path, int startIndex) {
        this.startIndex = startIndex;
        init(initPositions, path, startIndex);
    }

    public PathRadixTree(short[] initPositions, Path path) {
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
        for (int i = startIndex;i<startIndex + positions.size();++i) {
            if (checkPositions[i] > positions.get(i-startIndex)) {
                if (i-startIndex == positions.size() - 1) {
                    return right == null ? null : right.get(checkPositions);
                } else {
                    return null;
                }
            } else if (checkPositions[i] < positions.get(i-startIndex)) {
                if (i-startIndex == positions.size() - 1) {
                    return left == null ? null : left.get(checkPositions);
                } else {
                    return null;
                }
            }
        }
        if (match == null) {
            return data;
        } else {
            return match.get(checkPositions);
        }
    }

    public void put(short[] newPositions, Path path) {
        for (int i = startIndex; i < startIndex + positions.size(); ++i) {
            if (newPositions[i] > positions.get(i - startIndex)) {
                if (i == positions.size() - 1) {
                    if (right == null) {
                        right = new PathRadixTree(newPositions, path, i);
                        return;
                    } else {
                        right.put(newPositions, path);
                        return;
                    }
                } else {
                    branchRight(newPositions, path, i);
                    return;
                }
            } else if (newPositions[i] < positions.get(i - startIndex)) {
                if (i == positions.size() - 1) {
                    if (left == null) {
                        left = new PathRadixTree(newPositions, path, i);
                        return;
                    } else {
                        left.put(newPositions, path);
                        return;
                    }
                } else {
                    branchLeft(newPositions, path, i);
                    return;
                }
            }
        }
        if (match == null) {
            data.add(path);
        } else {
            match.put(newPositions, path);
        }
    }

    private void branch(short[] newPositions, Path path, int index, boolean branchRight) {
        List<Short> newMatchList = positions.subList(index-startIndex+1, positions.size());
        List<Short> myNewList = positions.subList(0, index-startIndex+1);
        List<Short> newBranchList = new ArrayList<>(newPositions.length - index);
        for (int i = index;i<newPositions.length;++i) {
            newBranchList.add(newPositions[i]);
        }
        this.match = PathRadixTree.builder()
                .positions(newMatchList)
                .data(data)
                .startIndex(index+1)
                .left(left)
                .right(right)
                .match(match)
                .build();
        this.data = null;
        this.left = null;
        this.right = null;
        this.positions = myNewList;
        List<Path> branchData = new ArrayList<>();
        branchData.add(path);
        PathRadixTree branch = PathRadixTree.builder()
                .positions(newBranchList)
                .data(branchData)
                .startIndex(index)
                .build();
        if (branchRight) {
            right = branch;
        } else {
            left = branch;
        }

    }

    private void branchLeft(short[] newPositions, Path path, int index) {
        branch(newPositions, path, index, false);
    }

    private void branchRight(short[] newPositions, Path path, int index) {
       branch(newPositions, path, index, true);
    }
}
