# Fibonacci Heap (Java)

> A fast, pointer-based **Fibonacci Heap** for positive integer keys — with full
> support for `insert`, `findMin`, `deleteMin`, `decreaseKey`, `delete`, and `meld`.
> Tracks **total links/cuts** and maintains **amortized** performance guarantees.

<p align="center">
  <img alt="Fibonacci Heap sketch" src="https://user-images.githubusercontent.com/placeholder/fib-heap.png" width="520">
</p>

## ✨ Highlights
- **Amortized O(1):** `insert`, `findMin`, `meld`, `decreaseKey`  
- **Amortized O(log n):** `deleteMin`, `delete`
- **True Fibonacci-heap structure:** circular doubly-linked root list, child lists,
  lazy consolidation, cascading cuts
- **Stats built in:** `totalLinks()` and `totalCuts()` for analysis/assignments
- **Compact API:** single `FibonacciHeap` class with an inner `HeapNode`

---

## Class Overview

```java
public class FibonacciHeap {
    public static class HeapNode {
        public int key;
        public String info;   // optional payload
        // pointers: parent, child, prev, next
        // metadata: rank, mark
    }

    public HeapNode insert(int key, String info)          // O(1) amortized
    public HeapNode findMin()                             // O(1)
    public void      deleteMin()                          // O(log n) amortized
    public void      decreaseKey(HeapNode x, int diff)    // O(1) amortized
    public void      delete(HeapNode x)                   // O(1) amortized, worst-case O(log n)
    public void      meld(FibonacciHeap other)            // O(1)
    public int       size()
    public int       numTrees()
    public int       totalLinks()
    public int       totalCuts()
}
```

## Quick Start
```
public class Demo {
    public static void main(String[] args) {
        FibonacciHeap h = new FibonacciHeap();

        FibonacciHeap.HeapNode a = h.insert(8,  "eight");
        FibonacciHeap.HeapNode b = h.insert(3,  "three");
        FibonacciHeap.HeapNode c = h.insert(21, "twenty-one");

        System.out.println("min = " + h.findMin().key); // 3

        // Decrease a key and watch cascading cuts happen when needed
        h.decreaseKey(c, 20); // 21 -> 1
        System.out.println("min = " + h.findMin().key); // 1

        // Pop minimums
        h.deleteMin(); // removes key 1
        h.deleteMin(); // removes key 3

        System.out.println("size = " + h.size()); // 1
        System.out.println("#links = " + h.totalLinks() + ", #cuts = " + h.totalCuts());
    }
}
```

Compile & run:
```bash
javac FibonacciHeap.java Demo.java
java Demo
```

## Time Complexity (Amortized)
| Operation     | Complexity                       |
| ------------- | -------------------------------- |
| `insert`      | **O(1)**                         |
| `findMin`     | **O(1)**                         |
| `meld`        | **O(1)**                         |
| `decreaseKey` | **O(1)**                         |
| `deleteMin`   | **O(log n)**                     |
| `delete`      | **O(1)** avg, **O(log n)** worst |

These bounds follow the classical Fibonacci-heap analysis (lazy consolidation, marking, and cascading cuts)


## Implementation Notes

Root list: circular doubly linked; min points at the current global minimum.

Children: each node keeps a circular child list and its rank (#children).

Consolidation: merges equal-rank roots after deleteMin using a rank table.

Cuts: cut(x, y) and cascadingCut(y) implement the marking discipline.

Instrumentation: totalLinks and totalCuts are updated in link and cut.

## Invariants / Assumptions

Keys are positive integers (as specified).

decreaseKey(x, diff) requires diff > 0 and x != null.

delete(x) is implemented via decreaseKey(x, x.key - 1) followed by a min-delete.

## Suggestions for Tests

Testa is a scrap file which we used for testing, you're welcome to use it. Some more suggestions: 

Insert many items; verify nondecreasing sequence of deleteMin().

Random decreaseKey operations; assert findMin() matches the known minimum.

Meld two heaps and verify the size/min correctness.

Track totalLinks() / totalCuts() under targeted scenarios.
