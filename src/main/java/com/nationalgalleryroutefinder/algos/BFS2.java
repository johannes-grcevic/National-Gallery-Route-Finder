package com.nationalgalleryroutefinder.algos;

import com.nationalgalleryroutefinder.model.MyArrayList;

import java.awt.image.BufferedImage;
import java.util.*;

public final class BFS2 {

    public record PointOnGraph(int x, int y) {}

    // returns shortest path using BFS
    public static List<PointOnGraph> traverse(BufferedImage image, PointOnGraph start, PointOnGraph end) {

        List<PointOnGraph> correctPath = new MyArrayList<>();

        Set<PointOnGraph> visited = new HashSet<>();
        visited.add(start);

        Queue<PointOnGraph> queue = new LinkedList<>();
        queue.add(start);


        Map<PointOnGraph, PointOnGraph> parent = new HashMap<>();
        parent.put(start, null);

        boolean found = false;


        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        // actual bfs traversal
        while (!queue.isEmpty()) {

            PointOnGraph current = queue.poll();

            if (current.equals(end)) {
                found = true;
                break;
            }

            for (int[] dir : directions) {
                int nextX = current.x + dir[0];
                int nextY = current.y + dir[1];

                if (nextX < 0 || nextY < 0 || nextX >= image.getWidth() || nextY >= image.getHeight()) continue;

                int argb = image.getRGB(nextX, nextY);
                int rgb = argb & 0xFFFFFF;
                if (rgb < 0x808080) continue;

                PointOnGraph neighbour = new PointOnGraph(nextX, nextY);

                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                    parent.put(neighbour, current);
                }
            }
        }

        if (!found) return correctPath;


        List<PointOnGraph> reversePath = new MyArrayList<>();

        PointOnGraph current = end;
        while (current != null) {
            reversePath.add(current);
            current = parent.get(current);
        }

        for (int i = reversePath.size() - 1; i >= 0; i--) {
            correctPath.add(reversePath.get(i));
        }

        return correctPath;
    }

    
}
