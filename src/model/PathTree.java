package model;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class PathTree {

    private List<Short> positions;
    private PathTree right;
    private PathTree left;
    private PathTree match;
    private List<Path> data;
    private int startIndex;

    public PathTree(short[] initPositions, Path path, int startIndex) {
        this.startIndex = startIndex;
        init(initPositions, path, startIndex);
    }

    public PathTree(short[] initPositions, Path path) {
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
                if (i == positions.size() - 1) {
                    return right == null ? null : right.get(checkPositions);
                } else {
                    return null;
                }
            } else if (checkPositions[i] < positions.get(i-startIndex)) {
                if (i == positions.size() - 1) {
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
        for (int i = startIndex;i<startIndex + positions.size();++i) {
            if (newPositions[i] > positions.get(i-startIndex)) {
                if (i == positions.size() -1) {
                    if (right == null) {
                        right = new PathTree(newPositions, path, i+1);
                    } else {
                        right.put(newPositions, path);
                    }
                } else {
                    branchRight(newPositions, path, i+1);
                }
            } else if (newPositions[i] < positions.get(i-startIndex)) {
                if (i == positions.size() - 1) {
                    if (left == null) {
                        left = new PathTree(newPositions, path, i+1);
                    } else {
                        left.put(newPositions, path);
                    }
                } else {
                    branchLeft(newPositions, path, i+1);
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
        List<Short> newMatchList = positions.subList(index-startIndex, positions.size());
        List<Short> myNewList = positions.subList(0, index-startIndex);
        List<Short> newBranchList = new ArrayList<>(newPositions.length - index);
        for (int i = index;i<newPositions.length;++i) {
            newBranchList.add(newPositions[startIndex + i]);
        }
        match = PathTree.builder()
                .positions(newMatchList)
                .data(data)
                .startIndex(index)
                .left(left)
                .right(right)
                .match(match)
                .build();
        List<Path> rightData = new ArrayList<>();
        rightData.add(path);
        positions = myNewList;
        PathTree branch = PathTree.builder()
                .positions(newBranchList)
                .data(rightData)
                .startIndex(index)
                .build();
        if (branchRight) {
            right = branch;
            left = null;
        } else {
            left = branch;
            right = null;
        }

    }

    private void branchLeft(short[] newPositions, Path path, int index) {
        branch(newPositions, path, index, false);
    }

    private void branchRight(short[] newPositions, Path path, int index) {
       branch(newPositions, path, index, true);
    }
}
