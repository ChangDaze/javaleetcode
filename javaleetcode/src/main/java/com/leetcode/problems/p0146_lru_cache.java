package com.leetcode.problems;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class p0146_lru_cache {

    // Approach 1: Custom Doubly Linked List + HashMap (Classic interview implementation)
    public static class LRUCache {

        // declare doubly linked list, make it easy to move node and use prev maintain list's order
        private static class Node {
            int key;
            int value;
            Node prev;
            Node next;

            // for dummy node
            public Node () {}

            public Node(int key, int value) {
                this.key = key;
                this.value = value;
            }
        }

        // use hash map wire doubly linked list to approach search complexity O(1)
        private Map<Integer, Node> map;
        private int capacity;
        private Node head; // mru
        private Node tail; // lru

        public LRUCache(int capacity) {
            this.map = new HashMap<Integer, Node>();
            this.capacity = capacity;
            // dummy head and tail make it very easy to locate and modify real head and tail
            head = new Node();
            tail = new Node();
            head.next = tail;
            tail.prev = head;
        }

        private void addToHead(Node node) {
            // put in new node right after dummy head
            node.prev = head;
            node.next = head.next;

            head.next.prev = node;
            head.next = node;
        }

        private void removeNode(Node node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void moveToHead(Node node) {
            removeNode(node);
            addToHead(node);
        }

        private Node removeTail() {
            Node lru = tail.prev;
            removeNode(lru);
            return lru; // remove from linked list first then offer object to remove from hashmap
        }

        public int get(int key) {
            Node node = map.get(key);
            if (node == null) {
                return -1;
            }

            moveToHead(node); // key be used
            return node.value;
        }

        public void put(int key, int value) {
            // 1. update existing key
            Node node = map.get(key);
            if (node != null) {
                node.value = value;
                moveToHead(node); // key be used
                return;
            }

            // 2. put in new key and evict if over capacity
            Node newNode = new Node(key, value);
            map.put(key, newNode);
            addToHead(newNode);

            if (map.size() > capacity) {
               Node lru = removeTail();
               map.remove(lru.key);
            }
        }
    }

    // Approach 2: Using Java's built-in LinkedHashMap (Production solution)
    // Note: Java's LinkedHashMap already maintains a doubly linked list running through all its entries.
    // - accessOrder = true: orders elements from least-recently accessed to most-recently accessed (LRU order)
    // - removeEldestEntry: automatically evicts the LRU item when size exceeds capacity.
    public static class LRUCache2 extends LinkedHashMap<Integer, Integer> {
        private final int capacity;

        public LRUCache2(int capacity) {
            // super(initialCapacity, loadFactor, accessOrder):
            // 1. capacity: initial number of bucket slots.
            // 2. loadFactor (0.75f): controls when the internal bucket array is resized (rehashed).
            //    - threshold = capacity * loadFactor. When elements exceed threshold, capacity doubles.
            //    - 0.75 (75%) is Java's default optimal trade-off between space usage and collision avoidance.
            //    - 'f' specifies a 32-bit float literal (otherwise 0.75 defaults to a 64-bit double).
            // 3. accessOrder (true): enables LRU access-order (default false is insertion-order).
            super(capacity, 0.75f, true);
            this.capacity = capacity;
        }

        public int get(int key) {
            return super.getOrDefault(key, -1);
        }

        public void put(int key, int value) {
            super.put(key, value);
        }

        // Called automatically by put() and putAll() after inserting an entry
        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
            return size() > capacity;
        }
    }
}
