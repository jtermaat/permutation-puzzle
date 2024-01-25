package model;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
public class MoveNode {
    private final Move move;
    private MoveNode next;
    private final Set<MoveNode> options;

    @Setter
    private MoveNode backPointer;

    public MoveNode(Move move) {
        this.move = move;
        options = new HashSet<>();
    }

    public static MoveNode fromList(List<Move> moves) {
        MoveNode startNode = new MoveNode(moves.get(0));
        MoveNode lastNode = startNode;
        for (int i = 1;i<moves.size();++i) {
            MoveNode newNode = new MoveNode(moves.get(i));
            lastNode.setNext(newNode);
            lastNode = newNode;
        }
        return startNode;
    }

    public void createChainTo(MoveNode end, List<Move> moves) {
        MoveNode newNext = new MoveNode(moves.get(0));
        MoveNode lastNode = newNext;
        for (int i = 1;i<moves.size();++i) {
            MoveNode newNode = new MoveNode(moves.get(i));
            lastNode.setNext(newNode);
            lastNode = newNode;
        }
        while (next != end) {
            next = next.getNext();
        }
        lastNode.setNext(end);
        next = newNext;
    }

    public List<Move> tolist() {
        List<Move> moveList = new ArrayList<>();
        MoveNode pointer = this;
        while (pointer != null) {
            moveList.add(pointer.getMove());
            pointer = pointer.getNext();
        }
        return moveList;
    }

    public void setNext(MoveNode next) {
        this.next = next;
        options.add(next);
    }

    public void optimizeRoute() {
        Queue<MoveNode> nodeQueue = new LinkedList<>();
        nodeQueue.offer(this);
        while (!nodeQueue.isEmpty()) {
            MoveNode node = nodeQueue.poll();
            if (node.getNext() == null) {
                while(node.getBackPointer() != null) {
                    node.getBackPointer().setNext(node);
                    node = node.getBackPointer();
                }
                return;
            }
            Set<MoveNode> allNextOptions = node.getOptions();
            for (MoveNode option : allNextOptions) {
                if (option.getBackPointer() == null) {
                    option.setBackPointer(node);
                    nodeQueue.offer(option);
                }
            }
        }
        System.out.println("An error occurred and the end of the sequence was never found.");
    }
}
