import java.util.Arrays;

public class Testa {
	public static void main(String[] args) {
		FibonacciHeap heap = new FibonacciHeap();
		FibonacciHeap.HeapNode node2 = heap.insert(2, "br");
		FibonacciHeap.HeapNode node3 = heap.insert(3, "br");
		FibonacciHeap.HeapNode node4 = heap.insert(4, "br");
		FibonacciHeap.HeapNode node5 = heap.insert(5, "br");
		FibonacciHeap.HeapNode node6 = heap.insert(6, "br");
		FibonacciHeap.HeapNode node7 = heap.insert(7, "br");
		heap.deleteMin();
		System.out.println(node4.rank);
		System.out.println(heap.numTrees());
		heap.delete(node6);
		System.out.println(node4.rank);
		System.out.println(heap.numTrees());
		System.out.println(heap.totalCuts());
	}
}