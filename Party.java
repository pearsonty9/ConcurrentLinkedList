import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Party {
  public static final int size = 500000;
  public static ConcurrentLinkedList list = new ConcurrentLinkedList();
  public static List<Integer> giftBag = new ArrayList<Integer>(size);
  public static AtomicInteger counter = new AtomicInteger(0);
  public static AtomicInteger removed = new AtomicInteger(0);
  public static boolean done = false;
  public static void main(String[] args) {
    Party p = new Party();

    long start = System.currentTimeMillis();
    for (int i = 0; i < size; i++)
      giftBag.add(i);

    Collections.shuffle(giftBag);
    for (int i = 0; i < 4; i++) {
      Servant s = new Servant(p);
      s.start();
    }

    while(removed.get() < size) continue;
    done = true;
    long end = System.currentTimeMillis();
    System.out.println(end - start + "ms");
    System.out.println("All done sending thank you letters");
  }


}

class ConcurrentLinkedList {

  Node head;
  Node tail;

  ConcurrentLinkedList() {
    this.head = new Node((int)-1e9);
    this.tail = new Node((int)1e9);

    this.head.next = new AtomicMarkableReference<Node>(tail, false);
    this.tail.next = new AtomicMarkableReference<Node>(null, false);
  }

  public boolean add(int item) {
    int key = item;
    while (true) {
      Window window = find(head, key);
      Node pred = window.pred, curr = window.curr;
      if (curr.key == key) {
        return false;
      }
      else {
        Node node = new Node(item);
        node.next = new AtomicMarkableReference<Node>(curr, false);
        if (pred.next.compareAndSet(curr, node, false, false)) {
          return true;
        }
      }
    }
  }

  public boolean removeFirst() {
    if (head.next.getReference() == tail)
      return false;
    while (true) {
      Node pred = head, curr = head.next.getReference();
      if (curr == tail) {
        return false;
      }
      else {
        Node succ = curr.next.getReference();
        if (pred.next.compareAndSet(curr, succ, false, false))
          return true;
      }
    }
  }

  public boolean contains(int item) {
    int key = item;
    boolean marked[] = {false};
    Node curr = head;
    while (curr.key < key) {
      curr = curr.next.getReference();
      Node succ = curr.next.get(marked);
    }
    return (curr.key == key && !marked[0]);
  }

  public void print() {
    Node curr = head;
    boolean isMarked = false;
    while(curr.key < 1e9) {
      System.out.println(curr.item + " " + isMarked);
      curr = curr.next.getReference();
      isMarked = curr.next.isMarked();
    }
  }

  public Window find(Node head, int key) {
    Node pred = null, curr = null, succ = null;
    boolean[] marked = {false};
    boolean snip;
    retry: while (true) {
      pred = head;
      curr = pred.next.getReference();
      while (true) {
        succ = curr.next.get(marked);
        while (marked[0]) {
          snip = pred.next.compareAndSet(curr, succ, false, false);
          if (!snip) continue retry;
          curr = succ;
          succ = curr.next.get(marked);
        }
        if (curr.key >= key) {
          return new Window(pred, curr);
        }
        pred = curr;
        curr = succ;
      }
    }
  }

  private class Window {
    public Node pred, curr;
    Window(Node pred, Node curr) {
        this.pred = pred;
        this.curr = curr;
    }
  }

  private class Node {
    int item;
    int key;
    AtomicMarkableReference<Node> next;

    Node(int item) {
      this.item = item;
      this.key = item;
      this.next = new AtomicMarkableReference<Node>(null, false);
    }
  }
}
