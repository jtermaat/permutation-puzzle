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
        try {
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
    }catch (OutOfMemoryError e) {
        System.out.println("Uh oh.");
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

    public static void main(String[] args) {
        short[] testPositions1 = new short[]{0,1,2,3,4,5,6,7,8,9,10};
        short[] testPositions2 = new short[]{0,2,4,1,3,5,6,7,8,9,10};
        short[] testPositions3 = new short[]{5,2,4,1,3,0,6,7,8,9,10};
        short[] testPositions4 = new short[]{6,2,4,1,3,5,0,7,8,9,10};
        short[] testPositions5 = new short[]{6,2,3,1,0,5,4,7,8,9,10};
        short[] testPositions6 = new short[]{6,3,4,1,3,5,0,7,8,9,10};
        short[] testPositions7 = new short[]{7,2,4,1,3,5,6,0,8,9,10};

        Path path1 = new Path("1");
        Path path2 = new Path("2");
        Path path3 = new Path("3");
        Path path4 = new Path("4");
        Path path5 = new Path("5");
        Path path6 = new Path("6");
        Path path7 = new Path("7");
        Path path8 = new Path("8");
        Path path9 = new Path("9");
        Path path10 = new Path("10");

        System.out.println("Putting them in the tree.");
        PathRadixTree tree = new PathRadixTree(testPositions3, path3);
        tree.put(testPositions2, path2);
        tree.put(testPositions4, path4);
        tree.put(testPositions2, path8);
        tree.put(testPositions7, path7);
        tree.put(testPositions6, path6);
        tree.put(testPositions2, path9);
        tree.put(testPositions1, path1);
        tree.put(testPositions5, path5);
        tree.put(testPositions4, path10);
        System.out.println("Testing retrival.");
        System.out.println("For testPositions5, expecting path5.");
        System.out.println(tree.get(testPositions5));
        System.out.println("For testPositions2, expecting list of path2, path8, and path9.");
        System.out.println(tree.get(testPositions2));
    }
}
