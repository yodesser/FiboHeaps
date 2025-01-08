import java.util.Arrays;

public class Testa {
	public static void main(String[] args) {
		FibonacciHeap heap = new FibonacciHeap();
		heap.insert(1, "br");
		heap.insert(12, "br");
		heap.insert(415, "br");
		heap.insert(421, "br");
		heap.insert(456, "br");
		heap.insert(1048, "br");
		heap.printHeap();

		heap.consolidate();
		heap.printHeap();

		heap.deleteMin();
		heap.printHeap();

		heap.insert(0, "sk");
		heap.consolidate();
		heap.printHeap();

		heap.decreaseKey(heap.min.child.next, 2500);
		heap.printHeap();
		System.out.println(heap.totalCuts);
		System.out.println(heap.totalLinks);

		heap.decreaseKey(heap.min.next.child, 2500);
		heap.printHeap();
		System.out.println(heap.totalCuts);
		System.out.println(heap.totalLinks);

		heap.cut(heap.min.next.child, heap.min.next);
		heap.printHeap();
		System.out.println(heap.totalCuts);
		System.out.println(heap.totalLinks);
		System.out.println(heap.size);
		System.out.println(heap.numTrees);

		heap.delete(heap.min.next.child);
		heap.printHeap();

		FibonacciHeap heap2 = new FibonacciHeap();
		heap2.insert(1, "br");
		heap2.insert(12, "br");
		heap2.insert(415, "br");
		heap2.insert(421, "br");
		heap2.consolidate();

		heap.meld(heap2);
		heap.printHeap();
	}
}