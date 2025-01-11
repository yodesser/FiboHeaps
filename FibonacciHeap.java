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

	public void detachChildren(HeapNode node) { // O(node.rank) = O(logn)
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
	public HeapNode findMin() { //O(1)
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
	public void deleteMin() { //O(logn)
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


	public void consolidate() { //O(logn)
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


	public void link(HeapNode y, HeapNode x) { //O(1)
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

// Documentation deleteMin:
/**
 * deleteMin removes the global minimum node from the heap and performs the following steps:
 * 1) If the heap is empty, there is nothing to delete and the method returns.
 * 2) Otherwise, it detaches the children (if any) of the minimum node and adds them to the root list.
 * 3) It removes the minimum node from the root list and decreases the total size by one.
 * 4) If the heap is not empty after removal, it calls consolidate() to restore the Fibonacci Heap structure:
 *    - consolidate first counts how many roots are in the circular list and stores them in a temporary array (rootArray).
 *      This involves traversing the root list twice: once to count and once to populate the array.
 *    - It then merges any roots of the same rank (via link operations) and rebuilds a new root list, updating the min pointer.
 *
 * Time complexity:
 * - detachChildren costs O(r) = O(log n), where r is the rank (number of children) of the deleted node.
 * - consolidate involves creating and iterating over the rootArray of size O(log n) (since a Fibonacci Heap has O(log n) roots),
 *   merging them in O(1) per link, and finally constructing the new root list and finding the min in another O(log n) pass.
 * - Thus, deleteMin overall runs in O(log n) time.
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * pre: 0<diff<x.key
	 * <p>
	 * Decrease the key of x by diff and fix the heap.
	 */
	public void decreaseKey(HeapNode x, int diff) {  //O(1)
		if (diff <= 0 || x == null) {
			return;
		}

		x.key -= diff;

		HeapNode parent = x.parent;
		if (parent != null && x.key < parent.key) {
			// x now violates the min-heap property
			cut(x, parent);
			cascadingCut(parent);
		}

		// Update global min pointer if needed
		if (x.key < this.min.key) {
			this.min = x;
		}
	}

	public void cut(HeapNode x, HeapNode y) { //O(1)
		// Remove x from y's child list
		// 1) fix sibling pointers in the child list
		if (x.next == x) {
			// x was the only child
			y.child = null;
		} else {
			// x had siblings
			x.next.prev = x.prev;
			x.prev.next = x.next;

			// If x was y.child pointer, move child pointer
			if (y.child == x) {
				y.child = x.next;
			}
		}
		y.rank--;

		// 2) Add x as a root in the top-level list
		x.parent = null;
		x.mark = false; // unmark the node that has just been cut

		// Insert x into the root circular list (just like in `insert`)
		addToRootList(x);

		// The problem statement tracks total cuts:
		this.totalCuts++;
	}


	private void cascadingCut(HeapNode y) { //O(1)
		HeapNode z = y.parent;
		if (z != null) {
			if (!y.mark) {
				// First cut from below?
				y.mark = true;
			} else {
				// `y` was already marked:
				cut(y, z);
				cascadingCut(z);
			}
		}
	}

	// Documentation decreaseKey:
/**
 * decreaseKey(x, d) takes a node x whose key is reduced by d. After decreasing
 * its key, if x violates the heap property (its key is now smaller than its
 * parent's key), x is cut from its parent and placed into the root list.
 * This may trigger cascading cuts: if the parent was already marked, it is
 * also cut from its parent, and so on up the tree.
 *
 * Time complexity:
 * - Decreasing x.key itself is O(1).
 * - Checking x against its parent is O(1).
 * - Cutting x from its parent is O(1) amortized, and placing x in the root list is also O(1).
 * - Cascading cuts can propagate up the tree, but each node can only be cut once before
 *   it must be reattached and marked again. This ensures the operation is amortized O(1).
 *
 * Hence, the amortized time complexity for decreaseKey is O(1).
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Delete the x from the heap.
	 */
	public void delete(HeapNode x) { // O(logn) when x is the min, O(1) otherwise
		if (x == null) {
			return; // Nothing to delete
		}

		if (x == this.min) {
			// If x is the minimum, proceed with the standard deleteMin process
			deleteMin();
			return;
		}

		// Remove x from its parent's child list (if it has a parent)
		if (x.parent != null) {
			HeapNode parent = x.parent;
			cut(x, parent);
			cascadingCut(parent);
		}

		// detach x's children (if it has)
		if (x.child != null) {
			detachChildren(x);
		}

		// Remove x from the root list
		removeFromRootList(x);

		// Update the heap size
		this.size--;
	}

	/**
	 * Documentation for delete:
	 *
	 * This delete method removes a node x from the Fibonacci Heap.
	 * If x is the global minimum, it calls deleteMin to handle its removal,
	 * involving consolidation to restore the heap's structure. Otherwise, it performs
	 * the following steps:
	 *
	 * 1) If x has a parent, it is cut from its parent's child list using the cut method.
	 *    This is followed by cascadingCut to maintain heap properties.
	 * 2) x is then removed from the root list directly without triggering consolidation.
	 * 3) The heap size is decremented to reflect the removal of x.
	 *
	 * Time Complexity:
	 * - O(log n) when x is the minimum, as deleteMin involves consolidation.
	 * - O(1) when x is not the minimum, as the removal and cutting are constant-time operations.
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////



	/**
	 * Return the total number of links.
	 */
	public int totalLinks() { //O(1)
		return this.totalLinks;
	}


	/**
	 * Return the total number of cuts.
	 */
	public int totalCuts() { //O(1)
		return this.totalCuts;
	}



	/**
	 * Meld the heap with heap2
	 */
	public void meld(FibonacciHeap heap2) { //O(1)
		// 1) If heap2 is null or empty, nothing to do.
		if (heap2.isHeapEmpty()) {
			return;
		}

		// 2) If 'this' heap is empty, adopt heap2's data.
		if (this.isHeapEmpty()) {
			this.min = heap2.min;
			this.size = heap2.size;
			this.numTrees = heap2.numTrees;
			// Now clear out heap2 so it becomes redundant.
			heap2.min = null;
			heap2.size = 0;
			heap2.numTrees = 0;
			return;
		}

		// 3) Otherwise, both heaps are non-empty. We link their root lists.
		// Temporarily store neighbors
		HeapNode thisNext = this.min.next;    // Node after this.min
		HeapNode heap2Prev = heap2.min.prev;  // Node before heap2.min

		// Stitch the two circular lists together
		this.min.next = heap2.min;
		heap2.min.prev = this.min;

		heap2Prev.next = thisNext;
		thisNext.prev = heap2Prev;

		// Update global min if needed
		if (heap2.min.key < this.min.key) {
			this.min = heap2.min;
		}

		// Accumulate heap2's size and number of trees
		this.size += heap2.size;
		this.numTrees += heap2.numTrees;

		// 4) Clear out heap2
		heap2.min = null;
	}
	// Documentation meld:
/**
 *  Melds the current heap (this) with another FibonacciHeap (heap2).
 *  After calling meld, heap2 becomes redundant (effectively empty),
 *  and 'this' contains all elements from both heaps.
 *  The method does nothing but simple if-tests for edgecases and reassigning pointers consecutively. O(1).
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	/**
	 * Return the number of elements in the heap
	 */
	public int size() { //O(1)
		return this.size;
	} //O(1)


	/**
	 * Return the number of trees in the heap.
	 */
	public int numTrees() { return this.numTrees;} //O(1)

	// Documentation totalLinks & totalCuts & size & numTrees :
/**
 *  Having maintained proper fields all along, we just return them all in O(1)
 */
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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