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
	public int size;      // amount of nodes
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

		// Constructor
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
	 * Checks if the heap is empty.
	 */
	public boolean isHeapEmpty() { // O(1)
		return (this.min == null) && (this.size == 0);
	}

	/**
	 * Adds a node to the circular root list, right after this.min.
	 */
	public void addToRootList(HeapNode node) { // O(1)
		HeapNode temp = min.next;
		min.next = node;
		node.prev = min;
		node.next = temp;
		temp.prev = node;
		this.numTrees++;
	}

	/**
	 * Removes a node from the circular root list (assuming node is a root).
	 */
	public void removeFromRootList(HeapNode node) { // O(1)
		node.next.prev = node.prev;
		node.prev.next = node.next;
		this.numTrees--;
	}

	/**
	 * Detach all children of 'node' and move them into the root list.
	 */
	public void detachChildren(HeapNode node) { // O(node.rank) = O(log n) w.c.
		HeapNode curr = node.child;
		if (curr == null) {
			return;
		}

		HeapNode start = curr;
		do {
			HeapNode next = curr.next;

			// Move 'curr' to the root list
			addToRootList(curr);
			curr.parent = null;
			curr.mark = false;

			// In the first (correct) code, each child moved up
			// was considered a "cut" from its old parent:
			this.totalCuts++;

			curr = next;
		} while (curr != start);

		node.child = null;
		node.rank = 0;
	}

	/**
	 * Insert (key, info) into the heap and return the newly created node.
	 */
	public HeapNode insert(int key, String info) { // O(1)
		HeapNode newNode = new HeapNode(key, info);

		if (isHeapEmpty()) {
			min = newNode;
		} else {
			addToRootList(newNode);
			if (min.key > newNode.key) {
				min = newNode;
			}
		}
		this.size++;
		this.numTrees++;  // Matches the first code’s practice

		return newNode;
	}

	/**
	 * Return the node with the minimum key, or null if empty.
	 */
	public HeapNode findMin() { // O(1)
		if (isHeapEmpty()) {
			return null;
		}
		return min;
	}

	/**
	 * Remove the minimum node from the heap.
	 */
	public void deleteMin() { // O(log n)
		if (isHeapEmpty()) {
			return; // Nothing to delete
		}

		HeapNode oldMin = this.min;

		// 1) Move children of oldMin to the root list
		if (oldMin.child != null) {
			detachChildren(oldMin);
		}

		// 2) Remove oldMin from root list
		removeFromRootList(oldMin);
		this.size--;

		// 3) If that was the only node, the heap is now empty
		if (this.size == 0) {
			this.min = null;
			this.numTrees = 0;
			return;
		}

		// 4) Pick an arbitrary root as the new min, then consolidate
		this.min = oldMin.next;
		consolidate();
	}

	/**
	 * Merge roots of the same rank.
	 */
	public void consolidate() { // O(log n)
		if (this.min == null) {
			return;
		}

		// 1) Count how many roots we have in the list
		int rootCount = 1;
		HeapNode start = this.min;
		HeapNode current = this.min.next;
		while (current != start) {
			rootCount++;
			current = current.next;
		}

		// 2) Put them in a simple array
		HeapNode[] rootArray = new HeapNode[rootCount];
		rootArray[0] = this.min;
		int index = 1;
		current = this.min.next;
		while (current != start) {
			rootArray[index++] = current;
			current = current.next;
		}

		// 3) We’ll need at most ~ log2(size) ranks
		int maxRank = (int) Math.ceil(Math.log(this.size) / Math.log(2)) + 2;
		HeapNode[] rankTable = new HeapNode[maxRank];

		// 4) Merge duplicates
		for (int i = 0; i < rootCount; i++) {
			HeapNode x = rootArray[i];
			int r = x.rank;
			while (rankTable[r] != null) {
				HeapNode y = rankTable[r];
				if (y.key < x.key) {
					// swap
					HeapNode temp = x;
					x = y;
					y = temp;
				}
				link(y, x);
				rankTable[r] = null;
				r++;
			}
			rankTable[r] = x;
		}

		// 5) Rebuild root list and find new min
		this.min = null;
		this.numTrees = 0;

		for (HeapNode node : rankTable) {
			if (node == null) {
				continue;
			}
			node.parent = null;
			if (this.min == null) {
				this.min = node;
				node.prev = node;
				node.next = node;
			} else {
				// Insert node into the circular root list
				node.prev = this.min;
				node.next = this.min.next;
				this.min.next.prev = node;
				this.min.next = node;
				if (node.key < this.min.key) {
					this.min = node;
				}
			}
			this.numTrees++;
		}
	}

	/**
	 * Make y a child of x (both y and x are currently roots of equal rank).
	 */
	public void link(HeapNode y, HeapNode x) { // O(1)
		// Remove y from root list
		removeFromRootList(y);

		// Make y a child of x
		if (x.child == null) {
			x.child = y;
			y.prev = y;
			y.next = y;
		} else {
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
	 * Decrease the key of x by 'diff' and then fix the heap.
	 * (No full consolidation unless x actually becomes min and gets removed.)
	 */
	public void decreaseKey(HeapNode x, int diff) { // O(1) amortized
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

	/**
	 * Cut x from its parent y, making x a root in the top-level list.
	 */
	public void cut(HeapNode x, HeapNode y) { // O(1)
		// Remove x from y's child list
		if (x.next == x) {
			y.child = null;
		} else {
			x.next.prev = x.prev;
			x.prev.next = x.next;
			if (y.child == x) {
				y.child = x.next;
			}
		}
		y.rank--;

		// Add x as a new root
		x.parent = null;
		x.mark = false;

		addToRootList(x);

		// The first code also increments totalCuts here:
		this.totalCuts++;
	}

	/**
	 * Cascading cut if the parent was already marked.
	 */
	private void cascadingCut(HeapNode y) { // O(1) amortized
		HeapNode z = y.parent;
		if (z != null) {
			if (!y.mark) {
				y.mark = true;
			} else {
				cut(y, z);
				cascadingCut(z);
			}
		}
	}

	/**
	 * Delete the x from the heap.
	 */
	public void delete(HeapNode x) { // O(logn) w.c.
		if (x == null) {
			return;
		}
		if (x == this.min) {
			// If x is already the global minimum
			deleteMin();
		} else {
			// 1) If x has a parent, cut it out
			HeapNode parentNode = x.parent;
			if (parentNode != null) {
				cut(x, parentNode);
				cascadingCut(parentNode);
			}

			// 2) Detach all children of x
			detachChildren(x);

			// 3) Remove x itself from the root list
			removeFromRootList(x);
			this.size--;
		}
	}

	/**
	 * Return the total number of link operations (tree merges).
	 */
	public int totalLinks() { // O(1)
		return this.totalLinks;
	}

	/**
	 * Return the total number of cut operations.
	 */
	public int totalCuts() { // O(1)
		return this.totalCuts;
	}

	/**
	 * Meld the current heap with heap2 in O(1) time.
	 */
	public void meld(FibonacciHeap heap2) { // O(1)
		// 1) If heap2 is empty, do nothing
		if (heap2.isHeapEmpty()) {
			return;
		}

		// 2) If this heap is empty, adopt heap2
		if (this.isHeapEmpty()) {
			this.min = heap2.min;
			this.size = heap2.size;
			this.numTrees = heap2.numTrees;
			heap2.min = null;
			heap2.size = 0;
			heap2.numTrees = 0;
			return;
		}

		// 3) Both heaps non-empty. Merge their root lists.
		HeapNode thisNext = this.min.next;
		HeapNode heap2Prev = heap2.min.prev;

		this.min.next = heap2.min;
		heap2.min.prev = this.min;
		heap2Prev.next = thisNext;
		thisNext.prev = heap2Prev;

		// 4) Update min if needed
		if (heap2.min.key < this.min.key) {
			this.min = heap2.min;
		}

		// 5) Update size and numTrees
		this.size += heap2.size;
		this.numTrees += heap2.numTrees;

		// Clear out heap2
		heap2.min = null;
	}

	/**
	 * Return the number of elements in the heap
	 */
	public int size() { // O(1)
		return this.size;
	}

	/**
	 * Return the current number of trees in the heap
	 */
	public int numTrees() { // O(1)
		return this.numTrees;
	}

}
