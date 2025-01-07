/**
 * FibonacciHeap
 * <p>
 * An implementation of Fibonacci heap over positive integers.
 */
public class FibonacciHeap {
	public int size; //amount of nodes
	public HeapNode min;
	public int numTrees;
	public int totalLinks;
	public int totalCuts;

	/**
	 * Class implementing a node in a Fibonacci Heap.
	 */
	public static class HeapNode {
		public int key;
		public String info;
		public HeapNode child;
		public HeapNode next;
		public HeapNode prev;
		public HeapNode parent;
		public int rank;
		public boolean mark;


		//Constructor
		public HeapNode(int key, String info) {
			this.key = key;
			this.info = info;
			this.child = null;
			this.parent = null;
			this.next = this;
			this.prev = this;
			this.rank = 0;
			this.mark = false;
		}
	}


	/**
	 * A bunch of helper functions we'll need later on:
	 */
	public boolean isHeapEmpty() { //O(1)
		return (this.min == null) && (this.size == 0);
	}


	public void addToRootList(HeapNode node) { //O(1)
		HeapNode temp = min.next;
		min.next = node;
		node.prev = min;
		node.next = temp;
		temp.prev = node;
		this.numTrees++;
	}


	public void removeFromRootList(HeapNode node) { //O(1)
		node.next.prev = node.prev;
		node.prev.next = node.next;
		this.numTrees--;
	}

	public void detachChildren(HeapNode node) { // O(node.rank)
		HeapNode curr = node.child;
		if (curr == null) {
			return;
		}

		HeapNode start = curr;

		do {
			HeapNode next = curr.next;
			addToRootList(curr);
			curr.parent = null;
			curr.mark = false;
			curr = next;
		} while (curr != start);

		node.child = null;
		node.rank = 0;
	}

	/**
	 * pre: key > 0
	 * <p>
	 * Insert (key,info) into the heap and return the newly generated HeapNode.
	 * <p>
	 * Time complexity O(1)
	 */

	public HeapNode insert(int key, String info) { //O(1)
		HeapNode newNode = new HeapNode(key, info);

		if (isHeapEmpty()) {
			min = newNode;
		} else {
			addToRootList(newNode);
			if (min.key > newNode.key) {
				min = newNode;
			}
		}
		//not forgetting to update the size:
		this.size++;
		this.numTrees++;

		return newNode;
	}

// Documentation insert:
	/**
	 * Inserting a new node to a heap, we check whether the heap is empty. If so, we make our new and only node min, and
	 * create a circular linked list with only the one node. Otherwise, we insert it as a new root in the preexisting
	 * list, pushing it between the min and the following root. Lastly, we increment the size of the heap.
	 * The only operations we've used are the simplest ones - initializing new objects, reassigning pointers and
	 * incrementing an integer. The insert is indeed lazy, with time complexity of O(1)
	 *
	 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return the minimal HeapNode, null if empty
	 * <p>
	 * Time complexity O(1)
	 */
	public HeapNode findMin() {
		if (isHeapEmpty()) {
			return null;
		}
		return min;
	}
//Documentation findMin:
	/**
	 * The method does little to none - returning the pointer to a field which is maintained through all other methods.
	 * Time complexity is self-evidently O(1)
	 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Delete the minimal item
	 */
	public void deleteMin() {
		if (isHeapEmpty()) {
			return;
		}
		HeapNode oldMin = min;
		min = (min.next != min) ? min.next : min.child;

		// Store a reference to the next node in the root list
		HeapNode startNode = min;

		//Does min have kids?
		if (oldMin.child != null) {
			detachChildren(oldMin);
		}

		removeFromRootList(oldMin);

		size--;

		//is the heap empty after the removal of min?
		if (size == 0) {
			min = null;
			return;
		}
		consolidate(startNode);
	}


	public void consolidate(HeapNode startNode) {
		if (startNode == null) {
			return;
		}

		int maxRank = (int) Math.ceil(Math.log(size) / Math.log(2)) + 1;
		HeapNode[] table = new HeapNode[maxRank + 1];

		HeapNode curr = startNode;
		int count = 0;

		do {
			HeapNode next = curr.next;
			int rank = curr.rank;

			while (table[rank] != null) {
				HeapNode tennant = table[rank];
				curr = link(tennant, curr); // Merge trees
				table[rank] = null;
				rank++;
			}

			table[rank] = curr;
			curr = next;

			count++;
		} while (curr != startNode);

		// Rebuild the root list and update min
		min = null;
		for (HeapNode node : table) {
			if (node != null) {
				if (min == null || node.key < min.key) {
					min = node;
				}
			}
		}
	}



	public HeapNode link(HeapNode y, HeapNode x) {  //O(1)

		//y being attached to x
		if (y.key >= x.key) {
			//remove the inferior node (the one with bigger key)
			removeFromRootList(y);

			//pointer game:
			if (x.child != null) {
				HeapNode child = x.child;
				y.next = child.next;
				child.next.prev = y;
				child.next = y;
				y.prev = child;
			} else {
				x.child = y;
				y.next = y;
				y.prev = y;
			}

			//papa!
			y.parent = x;
			x.rank++;
			y.mark = false;
			totalLinks++;

			return x;


			//x being attached to y. for some reason the code didn't work properly when i did it without duplicating code
		} else {
			removeFromRootList(x);

			//pointer game:
			if (y.child != null) {
				HeapNode child = y.child;
				x.next = child.next;
				child.next.prev = x;
				child.next = x;
				x.prev = child;
			} else {
				y.child = x;
				x.next = x;
				x.prev = x;
			}

			//papa!
			x.parent = y;
			y.rank++;
			x.mark = false;
			totalLinks++;

			return y;
		}
	}
// Documentation insert:
	/**
	 * deleteMin, as we've implemented it, removes the minimum node, detaches its children (if any) to the root list,
	 * removes the node from the root list, consolidates the heap by merging trees of the same rank, and updates the
	 * minimum pointer to the smallest node in the new root list.
	 * Time complexity: since deleteMin() does nothing but simple O(1) operations, and calling its helper functions, all
	 * one by one, it's dominated by the complexity of the helpers:
	 * detachChildren (whose complexity is as the number of children of the input which is upper-bounded by log(n)).
	 * consolidate opens an array of HeapNodes (O(1)) and proceeds to iterate over the rootlist, which initially contains
	 * O(logn) trees (due to the heap's structure). Each merge (via link) takes O(1). Finally, finding the new minimum
	 * during consolidate also involves iterating through the root list, adding another O(logn) steps. so the total work
	 * in consolidate is O(logn), and so is the complexity of deleteMin altogether.
	 *
	 *
	 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * pre: 0<diff<x.key
	 * <p>
	 * Decrease the key of x by diff and fix the heap.
	 */
	public void decreaseKey(HeapNode x, int diff) {
		return; // should be replaced by student code
	}

	/**
	 * Delete the x from the heap.
	 */
	public void delete(HeapNode x) {
		return; // should be replaced by student code
	}


	/**
	 * Return the total number of links.
	 */
	public int totalLinks() {
		return 0; // should be replaced by student code
	}


	/**
	 * Return the total number of cuts.
	 */
	public int totalCuts() {
		return 0; // should be replaced by student code
	}


	/**
	 * Meld the heap with heap2
	 */
	public void meld(FibonacciHeap heap2) {
		return; // should be replaced by student code
	}

	/**
	 * Return the number of elements in the heap
	 */
	public int size() {
		return 42; // should be replaced by student code
	}


	/**
	 * Return the number of trees in the heap.
	 */
	public int numTrees() {
		return 0; // should be replaced by student code
	}


	/**
	 * printer
	 */

	public void printHeap() {
		if (min == null) {
			System.out.println("The heap is empty.");
			return;
		}

		System.out.println("Fibonacci Heap (Root List):");
		HeapNode current = min;
		do {
			System.out.print("[" + current.key + "] → ");
			current = current.next;
		} while (current != min);
		System.out.println("(back to min)");

		System.out.println("\nTree Structure:");
		HeapNode root = min;
		do {
			printNode(root, "", true);
			root = root.next;
		} while (root != min);
	}

	private void printNode(HeapNode node, String prefix, boolean isTail) {
		if (node == null) {
			return;
		}

		// Print the current node
		System.out.println(prefix + (isTail ? "└── " : "├── ") + "[" + node.key + "]");

		// Recursively print children
		if (node.child != null) {
			HeapNode child = node.child;
			do {
				printNode(child, prefix + (isTail ? "    " : "│   "), child.next == node.child);
				child = child.next;
			} while (child != node.child);
		}
	}
}