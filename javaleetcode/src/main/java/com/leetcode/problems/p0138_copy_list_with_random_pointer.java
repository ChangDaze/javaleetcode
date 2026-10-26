package com.leetcode.problems;

import com.leetcode.common.Node;

import java.util.HashMap;
import java.util.Map;

public class p0138_copy_list_with_random_pointer {
    public Node copyRandomList(Node head) {
        HashMap<Node, Node> map = new HashMap<>();

        Node sourcePointer = head;
        Node copyVirtualHead = new Node(0);
        Node copyPointer = copyVirtualHead;
        // 1. copy the main flow and record the map
        while (sourcePointer != null){
            // copy node
            copyPointer.next = new Node(sourcePointer.val);

            // put on map
            map.put(sourcePointer, copyPointer.next);

            // move pointer
            copyPointer = copyPointer.next;
            sourcePointer = sourcePointer.next;
        }

        // 2. restart to handle random copy

        sourcePointer = head;
        // match source node so use next
        copyPointer = copyVirtualHead.next;

        while (sourcePointer != null){
            if(sourcePointer.random != null){
                copyPointer.random = map.get(sourcePointer.random);
            }
            copyPointer = copyPointer.next;
            sourcePointer = sourcePointer.next;
        }

        return copyVirtualHead.next;
    }

    public Node copyRandomList2(Node head) {
        if (head == null) return null;

        // interface is enough
        Map<Node, Node> map = new HashMap<>();

        // pass 1: clone all nodes into the map, with map we don't need wiring at pass 1
        Node curr = head;
        while (curr != null) {
            map.put(curr, new Node(curr.val));
            curr = curr.next;
        }

        // pass 2: connect both next and random
        curr = head;
        while (curr != null){
            // map.get(null) returns null automatically
            map.get(curr).next = map.get(curr.next);
            map.get(curr).random = map.get(curr.random);
            curr = curr.next;
        }

        return map.get(head);
    }

    public Node copyRandomList3(Node head) {
        if (head == null) return null;

        // 1. Interleave cloned nodes into the original list
        // from [A] -> [B] -> [C]
        // to [A] -> [A'] -> [B] -> [B'] --> [C] -> [C']
        Node curr = head;
        while (curr != null){
            Node clone = new Node(curr.val);
            clone.next = curr.next;
            curr.next = clone;
            curr = clone.next;
        }

        // 2. Wire the random for cloned nodes
        curr = head;
        while (curr != null){
            if (curr.random != null) {
                // curr.next.random = cloned node's random
                // curr.random.next = curr.random's next = cloned curr.random
                // source and destination both need to be cloned node
                curr.next.random = curr.random.next;
            }
            curr = curr.next.next;
        }

        // 3. Separate the two intertwined lists
        curr = head;
        Node cloneHead = head.next;
        while (curr != null){
            Node clone = curr.next;
            curr.next = clone.next;
            if (clone.next != null) {
                clone.next = clone.next.next;
            }
            curr = curr.next;
        }

        return cloneHead;
    }
}
