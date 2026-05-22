package com.nationalgalleryroutefinder.algos;

import com.nationalgalleryroutefinder.graph.Edge;
import com.nationalgalleryroutefinder.graph.Graph;
import com.nationalgalleryroutefinder.graph.Vertices;
import com.nationalgalleryroutefinder.model.MyArrayList;
import com.nationalgalleryroutefinder.model.MyHashtable;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;

public final class Dijkstra {

    // dijkstra algor which finds shortest weighted path between two rooms
    public static <T> List<T> traverse(Graph<T> graph, int startID, int endID, List<T> avoidedRooms) {
        List<T> result = new MyArrayList<>();
        Vertices<T> start = graph.getVertex(startID);
        Vertices<T> end = graph.getVertex(endID);
        if (start == null || end == null) return result;

        List<Vertices<T>> vertices = graph.getAllVertices();
        int size = vertices.size();

        MyHashtable<Integer, Integer> map = new MyHashtable<>();
        for (int i = 0; i < size; i++) {
            map.put(System.identityHashCode(vertices.get(i)), i);
        }

        // initialise all costs to infinity
        double[] cost = new double[size];
        int[] parent = new int[size];
        Arrays.fill(cost, Double.MAX_VALUE);
        Arrays.fill(parent, -1);

        // cost of reaching start from itself is 0
        cost[map.get(System.identityHashCode(start))] = 0;

        // min heap, always processes the cheapest known vertex next
        PriorityQueue<double[]> heap = new PriorityQueue<>(Comparator.comparingDouble(a -> a[1]));
        heap.offer(new double[]{map.get(System.identityHashCode(start)), 0});

        while (!heap.isEmpty()) {
            double[] current = heap.poll();
            int currentIndex = (int) current[0];
            double currentCost = current[1];

            // skip if we've already found a cheaper path to this vertex
            if (currentCost > cost[currentIndex]) continue;
            Vertices<T> currentVertex = vertices.get(currentIndex);
            // stop early if we've reached the destination
            if (currentVertex == end) break;

            // check each neighbour of the current vertex
            for (Edge<T> edge : currentVertex.getEdges()) {
                Vertices<T> neighbour = edge.getDestination();
                int neighbourIndex = map.get(System.identityHashCode(neighbour));
                // skip rooms the user wants to avoid
                if (avoidedRooms.contains(neighbour.getData())) continue;
                double newCost = currentCost + edge.getWeight();
                // if this path to neighbour is cheaper, update and add to heap
                if (newCost < cost[neighbourIndex]) {
                    cost[neighbourIndex] = newCost;
                    parent[neighbourIndex] = currentIndex;
                    heap.offer(new double[]{neighbourIndex, newCost});
                }
            }
        }

        // reconstruct path by walking back through parent array from end to start
        List<T> reversePath = new MyArrayList<>();
        int curr = map.get(System.identityHashCode(end));
        while (curr != -1) {
            reversePath.add(vertices.get(curr).getData());
            curr = parent[curr];
        }
        // reverse into correct order
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            result.add(reversePath.get(i));
        }
        return result;
    }

    // convenience method — just calls traverse with no avoided rooms
    public static <T> List<T> shortestPath(Graph<T> graph, int startID, int endID) {
        return traverse(graph, startID, endID, new MyArrayList<>());
    }

}