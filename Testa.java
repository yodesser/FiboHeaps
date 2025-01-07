public class Testa {
	public static void main(String[] args) {
		FibonacciHeap heap = new FibonacciHeap();
		heap.insert(1, "br");
		heap.insert(12, "br");
		heap.insert(4, "br");


		heap.insert(0, "br");

//		heap.consolidate(heap.min);
		heap.printHeap();

		heap.consolidate(heap.min);
		heap.printHeap();

		System.out.println("deletemin");
		heap.deleteMin();

		System.out.println(heap.min.key);

		heap.printHeap();

		System.out.println("deletemin");
		heap.deleteMin();

		System.out.println(heap.min.key);

		heap.printHeap();


		heap.insert(0, "br");
		heap.printHeap();

		System.out.println("deletemin");
		heap.deleteMin();

		System.out.println(heap.min.key);

		heap.printHeap();


		heap.insert(1, "br");
		heap.insert(12, "br");
		heap.insert(4, "br");


		System.out.println("deletemin");
		heap.deleteMin();

		System.out.println(heap.min.key);

		heap.printHeap();

		System.out.println("deletemin");
		heap.deleteMin();



		heap.printHeap();
	}
}