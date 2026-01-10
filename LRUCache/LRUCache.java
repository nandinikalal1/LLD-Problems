package LRUCache;
import java.util.HashMap;
import java.util.Map;
//Least Recently Used Cache
//A cache has limited size.When it becomes full and a new item is added, we must remove something.
//LRU rule: Remove the item that was used the longest time ago.
/*
 LRU Cache
 - get(key): O(1)
 - put(key, value): O(1)
 Design:
 - HashMap for fast lookup
 - Doubly Linked List to track usage order
   Head -> Most recently used
   Tail -> Least recently used
*/

public class LRUCache {

    // Doubly linked list node
    private static class Node {
        int key;
        int value;
        Node prev;
        Node next;

        Node(int key, int value) {
            this.key = key;
            this.value = value;
        }
    }

    private final int capacity;
    private final Map<Integer, Node> cache;

    // Dummy head and tail
    private final Node head;
    private final Node tail;

    public LRUCache(int capacity) {
        this.capacity = capacity;
        this.cache = new HashMap<>();

        head = new Node(-1, -1);
        tail = new Node(-1, -1);

        head.next = tail;
        tail.prev = head;
    }

    // Returns value if key exists, else -1
    public int get(int key) {
        Node node = cache.get(key);
        if (node == null) {
            return -1;
        }

        moveToFront(node);
        return node.value;
    }

    // Inserts or updates key-value pair
    public void put(int key, int value) {
        Node node = cache.get(key);

        if (node != null) {
            node.value = value;
            moveToFront(node);
            return;
        }

        if (cache.size() == capacity) {
            Node lru = tail.prev;
            removeNode(lru);
            cache.remove(lru.key);
        }

        Node newNode = new Node(key, value);
        addToFront(newNode);
        cache.put(key, newNode);
    }

    // Removes a node from the list
    private void removeNode(Node node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    // Adds node right after head (most recently used)
    private void addToFront(Node node) {
        node.next = head.next;
        node.prev = head;
        head.next.prev = node;
        head.next = node;
    }

    // Moves existing node to front
    private void moveToFront(Node node) {
        removeNode(node);
        addToFront(node);
    }

    // Simple test
    public static void main(String[] args) {
        LRUCache cache = new LRUCache(2);

        cache.put(1, 10);
        cache.put(2, 20);
        System.out.println(cache.get(1)); // 10

        cache.put(3, 30); // evicts key 2
        System.out.println(cache.get(2)); // -1

        cache.put(4, 40); // evicts key 1
        System.out.println(cache.get(1)); // -1
        System.out.println(cache.get(3)); // 30
        System.out.println(cache.get(4)); // 40
    }
}
   

