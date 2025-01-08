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
			return; // Nothing to delete
		}

		HeapNode oldMin = this.min;

		// 1) Move children of oldMin to the root list
		if (oldMin.child != null) {
			detachChildren(oldMin);
			// detachChildren() adds each child to the root list
			// and sets child's parent=null, node.child=null, etc.
		}

		// 2) Remove oldMin itself from the root list
		removeFromRootList(oldMin);
		this.size--;

		// 3) If that was the only node, heap becomes empty
		if (this.size == 0) {
			this.min = null;
			this.numTrees = 0;  // no trees left
			return;
		}

		// 4) Pick an arbitrary root as the new min (e.g., oldMin.next)
		//    We'll find the actual minimum in consolidate().
		this.min = oldMin.next;

		// 5) Consolidate to merge any trees of the same rank
		consolidate();
	}



	public void consolidate() {
		if (this.min == null) {
			return;
		}

		// 1) Count how many roots we currently have
		int rootCount = 1;
		HeapNode start = this.min;
		HeapNode current = this.min.next;
		while (current != start) {
			rootCount++;
			current = current.next;
		}

		// 2) Create a plain array for those roots
		HeapNode[] rootArray = new HeapNode[rootCount];

		// 3) Fill the array by traversing again
		rootArray[0] = this.min;
		int index = 1;
		current = this.min.next;
		while (current != start) {
			rootArray[index++] = current;
			current = current.next;
		}

		// (We now have each root exactly once in rootArray.)

		// 4) Create a rank table for merging
		int maxRank = (int) Math.ceil(Math.log(this.size) / Math.log(2)) + 2;
		HeapNode[] rankTable = new HeapNode[maxRank];

		// 5) Merge trees of the same rank
		for (int i = 0; i < rootCount; i++) {
			HeapNode x = rootArray[i];
			int r = x.rank;
			// continue linking while there's a root of the same rank
			while (rankTable[r] != null) {
				HeapNode y = rankTable[r];
				// ensure x is the root with the smaller key
				if (y.key < x.key) {
					HeapNode temp = x;
					x = y;
					y = temp;
				}
				link(y, x);        // y becomes child of x
				rankTable[r] = null;
				r++;
			}
			rankTable[r] = x;
		}

		// 6) Rebuild the root list from rankTable
		this.min = null;
		this.numTrees = 0;

		for (int i = 0; i < rankTable.length; i++) {
			if (rankTable[i] == null) {
				continue;
			}
			HeapNode node = rankTable[i];
			node.parent = null;

			// If no min yet, node becomes the only root
			if (this.min == null) {
				this.min = node;
				node.prev = node;
				node.next = node;
			} else {
				// Insert node into the circular root list (next to this.min)
				node.prev = this.min;
				node.next = this.min.next;
				this.min.next.prev = node;
				this.min.next = node;
				// Update min if needed
				if (node.key < this.min.key) {
					this.min = node;
				}
			}
			this.numTrees++;
		}
	}







	public void link(HeapNode y, HeapNode x) {
		// Remove y from the root list (we already know y is a root if rankTable had it).
		removeFromRootList(y);

		// Make y a child of x
		if (x.child == null) {
			x.child = y;
			y.prev = y;
			y.next = y;
		} else {
			// Insert y into x's child-list (circular)
			HeapNode c = x.child;
			y.prev = c;
			y.next = c.next;
			c.next.prev = y;
			c.next = y;
		}

		y.parent = x;
		x.rank++;
		y.mark = false;

		this.totalLinks++;
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