package com.nationalgalleryroutefinder.algos;

import com.nationalgalleryroutefinder.model.MyArrayList;
import com.nationalgalleryroutefinder.model.Point2DInt;

import java.awt.image.BufferedImage;
import java.util.*;

public final class BFSPixel {
    // returns shortest path using BFS
    public static List<Point2DInt> traverse(BufferedImage image, Point2DInt start, Point2DInt end) {

        List<Point2DInt> correctPath = new MyArrayList<>();

        Set<Point2DInt> visited = new HashSet<>();
        visited.add(start);

        Queue<Point2DInt> queue = new LinkedList<>();
        queue.add(start);

        Map<Point2DInt, Point2DInt> parent = new HashMap<>();
        parent.put(start, null);

        boolean found = false;

        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        // actual bfs traversal
        while (!queue.isEmpty()) {

            Point2DInt current = queue.poll();

            if (current.equals(end)) {
                found = true;
                break;
            }

            for (int[] dir : directions) {
                int nextX = current.x() + dir[0];
                int nextY = current.y() + dir[1];

                if (nextX < 0 || nextY < 0 || nextX >= image.getWidth() || nextY >= image.getHeight()) continue;

                int argb = image.getRGB(nextX, nextY);
                int rgb = argb & 0xFFFFFF;
                if (rgb < 0x808080) continue;

                Point2DInt neighbour = new Point2DInt(nextX, nextY);

                if (!visited.contains(neighbour)) {
                    visited.add(neighbour);
                    queue.add(neighbour);
                    parent.put(neighbour, current);
                }
            }
        }

        if (!found) return correctPath;


        List<Point2DInt> reversePath = new MyArrayList<>();

        Point2DInt current = end;
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
