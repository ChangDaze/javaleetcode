package com.leetcode.problems;

import com.leetcode.common.Node;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class p0138_copy_list_with_random_pointerTest {

    // ==================== Tests for copyRandomList ====================

    @Test
    void testNullHead() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();
        Node actual = solution.copyRandomList(null);
        assertNull(actual);
    }

    @Test
    void testSingleNodeSelfRandom() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // Node with val 1 pointing random to itself: [[1, 0]]
        Node head = new Node(1);
        head.random = head;

        Node copy = solution.copyRandomList(head);

        assertDeepCopyValid(head, copy);
    }

    @Test
    void testExample1() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[7,null],[13,0],[11,4],[10,2],[1,0]]
        Node n0 = new Node(7);
        Node n1 = new Node(13);
        Node n2 = new Node(11);
        Node n3 = new Node(10);
        Node n4 = new Node(1);

        n0.next = n1;
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;

        n0.random = null;
        n1.random = n0;
        n2.random = n4;
        n3.random = n2;
        n4.random = n0;

        Node copy = solution.copyRandomList(n0);

        assertDeepCopyValid(n0, copy);
    }

    @Test
    void testExample2() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[1,1],[2,1]]
        Node n0 = new Node(1);
        Node n1 = new Node(2);

        n0.next = n1;
        n0.random = n1;
        n1.random = n1;

        Node copy = solution.copyRandomList(n0);

        assertDeepCopyValid(n0, copy);
    }

    // ==================== Tests for copyRandomList2 ====================

    @Test
    void testNullHead2() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();
        Node actual = solution.copyRandomList2(null);
        assertNull(actual);
    }

    @Test
    void testSingleNodeSelfRandom2() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        Node head = new Node(1);
        head.random = head;

        Node copy = solution.copyRandomList2(head);

        assertDeepCopyValid(head, copy);
    }

    @Test
    void testExample2_1() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[7,null],[13,0],[11,4],[10,2],[1,0]]
        Node n0 = new Node(7);
        Node n1 = new Node(13);
        Node n2 = new Node(11);
        Node n3 = new Node(10);
        Node n4 = new Node(1);

        n0.next = n1;
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;

        n0.random = null;
        n1.random = n0;
        n2.random = n4;
        n3.random = n2;
        n4.random = n0;

        Node copy = solution.copyRandomList2(n0);

        assertDeepCopyValid(n0, copy);
    }

    @Test
    void testExample2_2() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[1,1],[2,1]]
        Node n0 = new Node(1);
        Node n1 = new Node(2);

        n0.next = n1;
        n0.random = n1;
        n1.random = n1;

        Node copy = solution.copyRandomList2(n0);

        assertDeepCopyValid(n0, copy);
    }

    // ==================== Tests for copyRandomList3 ====================

    @Test
    void testNullHead3() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();
        Node actual = solution.copyRandomList3(null);
        assertNull(actual);
    }

    @Test
    void testSingleNodeSelfRandom3() {
        // edge case test
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        Node head = new Node(1);
        head.random = head;

        Node copy = solution.copyRandomList3(head);

        assertDeepCopyValid(head, copy);
    }

    @Test
    void testExample3_1() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[7,null],[13,0],[11,4],[10,2],[1,0]]
        Node n0 = new Node(7);
        Node n1 = new Node(13);
        Node n2 = new Node(11);
        Node n3 = new Node(10);
        Node n4 = new Node(1);

        n0.next = n1;
        n1.next = n2;
        n2.next = n3;
        n3.next = n4;

        n0.random = null;
        n1.random = n0;
        n2.random = n4;
        n3.random = n2;
        n4.random = n0;

        Node copy = solution.copyRandomList3(n0);

        assertDeepCopyValid(n0, copy);
    }

    @Test
    void testExample3_2() {
        p0138_copy_list_with_random_pointer solution = new p0138_copy_list_with_random_pointer();

        // [[1,1],[2,1]]
        Node n0 = new Node(1);
        Node n1 = new Node(2);

        n0.next = n1;
        n0.random = n1;
        n1.random = n1;

        Node copy = solution.copyRandomList3(n0);

        assertDeepCopyValid(n0, copy);
    }

    /**
     * Helper method to verify that 'copy' is a true deep copy of 'original':
     * 1. All node values match.
     * 2. No cloned node shares memory references with original nodes (assertNotSame).
     * 3. 'next' and 'random' pointers in the cloned list point to the cloned nodes, not the original nodes.
     */
    private void assertDeepCopyValid(Node original, Node copy) {
        if (original == null) {
            assertNull(copy);
            return;
        }
        assertNotNull(copy);

        // Step 1: Map each original node to its corresponding copy, and verify val + references
        Map<Node, Node> originalToCopyMap = new HashMap<>();
        Node o = original;
        Node c = copy;

        while (o != null) {
            assertNotNull(c, "Cloned list is shorter than the original list");
            assertNotSame(o, c, "Cloned node must be a new object in memory, not the original node");
            assertEquals(o.val, c.val, "Cloned node val must match original node val");

            originalToCopyMap.put(o, c);

            o = o.next;
            c = c.next;
        }
        assertNull(c, "Cloned list is longer than the original list");

        // Step 2: Verify next and random pointers correctly point to the cloned nodes
        o = original;
        c = copy;
        while (o != null) {
            if (o.next != null) {
                assertSame(originalToCopyMap.get(o.next), c.next,
                        "Cloned 'next' pointer must point to the cloned node");
            } else {
                assertNull(c.next, "Cloned 'next' must be null when original 'next' is null");
            }

            if (o.random != null) {
                assertSame(originalToCopyMap.get(o.random), c.random,
                        "Cloned 'random' pointer must point to the cloned node, not original");
            } else {
                assertNull(c.random, "Cloned 'random' must be null when original 'random' is null");
            }

            o = o.next;
            c = c.next;
        }
    }
}
