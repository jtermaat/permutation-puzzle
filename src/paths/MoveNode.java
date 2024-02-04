package paths;


import lombok.Getter;
import lombok.Setter;
import model.Move;

import java.util.*;

@Getter
public class MoveNode {
    private final Move move;
    private MoveNode next;
    private final Set<MoveNode> options;

    @Setter
    private MoveNode backPointer;

    public MoveNode() {
        options = new HashSet<>();
        move = null;
    }

    public MoveNode(Move move) {
        this.move = move;
        options = new HashSet<>();
    }

    public MoveNode(List<Move> moves) {
        this(moves.get(0));
        MoveNode lastNode = this;
        for (int i = 1; i < moves.size(); ++i) {
            MoveNode newNode = new MoveNode(moves.get(i));
            lastNode.setNext(newNode);
            lastNode = newNode;
        }
    }

    public void createChainTo(MoveNode end, List<Move> moves) {
        if (backPointer != null) {
            MoveNode startNode = backPointer;
            if (moves.isEmpty()) {
                startNode.addOption(end);
            } else {
                MoveNode newNext = new MoveNode(moves.get(0));
                MoveNode lastNode = newNext;
                for (int i = 1; i < moves.size(); ++i) {
                    MoveNode newNode = new MoveNode(moves.get(i));
                    lastNode.addOption(newNode);
                    lastNode = newNode;
                }
                lastNode.addOption(end);
                startNode.addOption(newNext);
            }
        }
    }

    public List<Move> tolist() {
        return toList(null);
    }

    public List<Move> toList(MoveNode endNode) {
        List<Move> moveList = new ArrayList<>();
        MoveNode pointer = this;
        while (pointer != endNode) {
            moveList.add(pointer.getMove());
            pointer = pointer.getNext();
        }
        return moveList;
    }

    public void addOption(MoveNode option) {
        options.add(option);
        if (this.next == null) {
            this.next = option;
        }
    }

    public void setNext(MoveNode next) {
        this.next = next;
        options.add(next);
        next.setBackPointer(this);
    }

    public int getLength() {
        if (next == null) {
            return 1;
        } else {
            return next.getLength() + 1;
        }
    }

    public void printPathTo(MoveNode other) {
        MoveNode node = this;
        while (node != other) {
            System.out.print(node.getMove().getName());
            System.out.print(".");
            node = node.getNext();
        }
    }

    private void setBackPointersToNull() {
        MoveNode node = this;
        while (node != null) {
            node.setBackPointer(null);
            node = node.getNext();
        }
    }

    public void optimizeRoute() { // BFS to find the fastest route.
        setBackPointersToNull();
        Queue<MoveNode> nodeQueue = new ArrayDeque<>();
        nodeQueue.offer(this);
        while (!nodeQueue.isEmpty()) {
            MoveNode node = nodeQueue.poll();
            if (node.getNext() == null) {   // Means we found our end node.
                while(node.getBackPointer() != null) {
                    node.getBackPointer().setNext(node);
                    node = node.getBackPointer();
                }
                return;
            }
            Set<MoveNode> allNextOptions = node.getOptions();
            for (MoveNode option : allNextOptions) {
                if (option != null) {
                    if (option.getBackPointer() == null) {
                        option.setBackPointer(node);
                        nodeQueue.offer(option);
                    }
                }
            }
        }
    }
}
