/**
 * FibonacciHeap
 * 1:
 * Name: Iakov Odesser
 * Username: iakovodesser
 * ID: 209860188
 * 2:
 * Name: Eyal Sapir
 * Username: eyalsapir
 * ID: 206405417
 * An implementation of Fibonacci heap over positive integers.
 */

public class FibonacciHeap {
	public int size; //amount of nodes
	public HeapNode min;
	public int numTrees;
	public int totalLinks;
	public int totalCuts;


	/**
	 * Constructor to initialize an empty heap.
	 */
	public FibonacciHeap() {
	}


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


		/**
		 * HeapNode Constructor
		 */
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
			this.totalCuts++;
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

	/**
	 * Return the minimal HeapNode, null if empty.
	 */
	public HeapNode findMin() { //O(1)
		if (isHeapEmpty()) {
			return null;
		}
		return min;
	}


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

	/**
	 * Merges roots of the same rank and rebuilds the root list.
	 */
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

	/**
	 * Makes one root the child of another
	 */
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
		if (x.key <= this.min.key) {
			this.min = x;
		}
	}

	/**
	 * Cuts a node from its parent and makes it a new root
	 */
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

		// Insert x into the root circular list (just like in insert)
		addToRootList(x);

		// The problem statement tracks total cuts:
		this.totalCuts++;
	}

	/**
	 * Recursively cuts marked ancestors
	 */
	private void cascadingCut(HeapNode y) { //O(1)
		HeapNode z = y.parent;
		if (z != null) {
			if (!y.mark) {
				// First cut from below?
				y.mark = true;
			} else {
				// y was already marked:
				cut(y, z);
				cascadingCut(z);
			}
		}
	}


	/**
	 * Delete the x from the heap.
	 */
	public void delete(HeapNode x) { //Worst case O(logn), average O(1)
		if (x == null) {
			return; // Nothing to delete
		}
		if (x == this.min) {
			deleteMin();
			return;
		}

		HeapNode savedMin = this.min;
		int delta = x.key - 1;  // Enough to ensure x becomes <= all keys
		decreaseKey(x, delta);

		// Now x is the global minimum, so removing it is just deleteMin().
		deleteMinNocons(savedMin);
	}

	/**
	 * Removes the global min without consolidation
	 */
	public void deleteMinNocons(HeapNode savedMin) {
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

		// 4) make savedMin the new min
		this.min = savedMin;
	}

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

	/**
	 * Return the number of elements in the heap
	 */
	public int size() { //O(1)
		return this.size;
	}


	/**
	 * Return the number of trees in the heap.
	 */
	public int numTrees() {
		return this.numTrees;
	} //O(1)

	/**
	 *  Having maintained proper fields all along, we just return them all in O(1)
	 */
}