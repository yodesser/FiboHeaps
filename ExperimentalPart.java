import java.util.Random;

public class ExperimentalPart {
    public static void main(String[] args) {
        int[] iValues = {1, 2, 3, 4, 5}; // Values of i to test
        System.out.printf("%-5s %-20s %-15s %-15s %-15s %-15s\n", "i", "Avg Time (ms)", "Heap Size", "Connections", "Cuts", "Trees");
        System.out.println("-------------------------------------------------------------------------------");

        for (int i : iValues) {
            int n = (int) Math.pow(3, i + 7) - 1;
            long totalTime = 0;
            int totalHeapSize = 0;
            int totalConnections = 0;
            int totalCuts = 0;
            int totalTrees = 0;

            for (int experiment = 0; experiment < 20; experiment++) {
                FibonacciHeap heap = new FibonacciHeap();
                FibonacciHeap.HeapNode[] nodesArray = new FibonacciHeap.HeapNode[n]; // Array to store nodes

                // Precompute keys in descending order
                int[] keys = new int[n];
                for (int j = 0; j < n; j++) {
                    keys[j] = n - j; // Largest key first, smallest key last
                }

                // Insert nodes into the heap and store them in nodesArray
                long startTime = System.currentTimeMillis();
                for (int j = 0; j < n; j++) {
                    nodesArray[j] = heap.insert(keys[j], "Node " + j); // Insert keys in descending order
                }

                // Perform deleteMax and deleteMin alternately until heap size is 2**5 - 1
                heap.deleteMin();
                int heapSize = n - 1;
                int finalSize = (int) Math.pow(2, 5) - 1;
                int counter = 0; // Start with the largest element in nodesArray

                while (heapSize > finalSize) {
                    heap.delete(nodesArray[counter]); // Delete the node with the largest key
                    heap.deleteMin(); // Perform deleteMin
                    counter++; // Move to the next largest node
                    heapSize -= 2; // Two nodes removed in each iteration
                }

                long endTime = System.currentTimeMillis();

                // Collect metrics
                totalTime += (endTime - startTime);
                totalHeapSize += heap.size();
                totalConnections += heap.totalLinks();
                totalCuts += heap.totalCuts();
                totalTrees += heap.numTrees();
            }

            // Calculate averages
            double avgTime = totalTime / 20.0;
            int avgHeapSize = totalHeapSize / 20;
            int avgConnections = totalConnections / 20;
            int avgCuts = totalCuts / 20;
            int avgTrees = totalTrees / 20;

            // Print results for this i
            System.out.printf("%-5d %-20.2f %-15d %-15d %-15d %-15d\n", i, avgTime, avgHeapSize, avgConnections, avgCuts, avgTrees);
        }
    }
}
