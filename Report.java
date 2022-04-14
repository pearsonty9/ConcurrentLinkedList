import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class Report {
  public static ConcurrentLinkedList readings = new ConcurrentLinkedList();
  public static AtomicBoolean lock = new AtomicBoolean(false);
  public static AtomicInteger max = new AtomicInteger((int)-1e9);
  public static AtomicInteger min = new AtomicInteger((int)1e9);
  public static int maxDiff = 0;
  public static int interval = 0;
  public static boolean done = false;
  public static int time = 0;

  // How often to take readings in ms
  public static final int readingWaitTime = 1;
  // The number of times readings are taken before printing a report
  public static final int readingsPerReport = 60;
  // The max number of reports to be taken
  public static final int maxReports = 2;
  public static int reports = 0;

  public static void main(String[] args) throws InterruptedException {
    Report report = new Report();
    ArrayList<Sensor> threads = new ArrayList<Sensor>();
    for (int i = 0; i < 8; i++) {
      Sensor s = new Sensor(report);
      s.start();
    }

    while (reports < maxReports) {
      readings = new ConcurrentLinkedList();
      time = maxDiff = 0;
      while(time < readingsPerReport) {
        Thread.sleep(readingWaitTime);
        int diff =  max.getAndSet((int)-1e9) - min.getAndSet((int)1e9);
        if (diff > maxDiff) {
          interval = time;
          maxDiff = diff;
        }
        time++;
        while(lock.compareAndSet(true, false)) continue;
      }
      report.printReport(reports);
      reports++;
    }
    done = true;
  }

  public void printReport(int reportNumber) {
    System.out.println("------ Report #" + (reportNumber + 1) + " -------");
    System.out.println("  Smallest\tLargest");
    System.out.println("1: " + readings.removeFirst() + "\t\t " + readings.removeLast());
    System.out.println("2: " + readings.removeFirst() + "\t\t " + readings.removeLast());
    System.out.println("3: " + readings.removeFirst() + "\t\t " + readings.removeLast());
    System.out.println("4: " + readings.removeFirst() + "\t\t " + readings.removeLast());
    System.out.println("5: " + readings.removeFirst() + "\t\t " + readings.removeLast());
    System.out.println("Largest Temperature Difference: " + maxDiff + " @ " + interval + " to " + (interval+1));
    System.out.println();
  }
}

class ConcurrentLinkedList {

  Node head;
  Node tail;

  ConcurrentLinkedList() {
    this.head = new Node((int)-1e9, 0);
    this.tail = new Node((int)1e9, 60);

    this.head.next = new AtomicMarkableReference<Node>(tail, false);
    this.tail.next = new AtomicMarkableReference<Node>(null, false);
  }

  public boolean add(int item, int start) {
    while (true) {
      Window window = find(head, item);
      Node pred = window.pred, curr = window.curr;
      Node node = new Node(item, start);
      node.next = new AtomicMarkableReference<Node>(curr, false);
      if (pred.next.compareAndSet(curr, node, false, false)) {
        return true;
      }
    }
  }

  public int removeFirst() {
    if (head.next.getReference() == tail)
      return (int)-1e9;
    while (true) {
      Node pred = head, curr = head.next.getReference();
      if (curr == tail) {
        return (int)-1e9;
      }
      else {
        Node succ = curr.next.getReference();
        if (pred.next.compareAndSet(curr, succ, false, false))
          return curr.item;
      }
    }
  }

  public int removeLast() {
    boolean snip;
    while (true) {
      Window window = findLast(head, 71);
      Node pred = window.pred, curr = window.curr;
      // System.out.println("Pred: " + pred.item + " " + curr.item);
      if (pred == head || curr == tail) {
        return (int)-1e9;
      }
      else {
        Node succ = curr.next.getReference();
        snip = curr.next.attemptMark(succ, true);
        if (!snip) continue;
        pred.next.compareAndSet(curr, succ, false, false);
        return curr.item;
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
    Node curr = head.next.getReference();
    boolean isMarked = false;
    while(curr.key < 1e9) {
      System.out.println(curr.item + " " + curr.start);
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

  public Window findLast(Node head, int key) {
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
        // System.out.println(curr.item);
        if (curr.next.getReference().key > key) {
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
    int start;
    AtomicMarkableReference<Node> next;

    Node(int item, int start) {
      this.item = item;
      this.key = item;
      this.start = start;
      this.next = new AtomicMarkableReference<Node>(null, false);
    }
  }
}
