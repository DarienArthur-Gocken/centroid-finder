package io.github.darienarthurgocken.centroidfinder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class BfsBinaryGroupFinder implements BinaryGroupFinder {

    @Override
    public List<Group> findConnectedGroups(int[][] image) {
        if (image == null) {
            throw new NullPointerException();
        }

        if (image.length == 0) {
            throw new IllegalArgumentException();
        }

        int rows = image.length;
        int cols = image[0].length;

        boolean[][] visited = new boolean[rows][cols];
        List<Group> groups = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            if (image[row] == null || image[row].length != cols) {
                throw new IllegalArgumentException();
            }

            for (int col = 0; col < cols; col++) {
                if (image[row][col] == 1 && !visited[row][col]) {

                    int[] result = bfs(image, row, col, visited);

                    int size = result[0];
                    int sumX = result[1];
                    int sumY = result[2];

                    int centroidX = sumX / size;
                    int centroidY = sumY / size;

                    groups.add(
                        new Group(size, new Coordinate(centroidX, centroidY))
                    );
                }
            }
        }

        Collections.sort(groups, Collections.reverseOrder());

        return groups;
    }

    private int[] bfs(int[][] image, int startRow, int startCol, boolean[][] visited) {

        Queue<int[]> queue = new ArrayDeque<>();

        queue.offer(new int[]{startRow, startCol});
        visited[startRow][startCol] = true;

        int size = 0;
        int sumX = 0;
        int sumY = 0;

        int[][] directions = {
            {-1, 0}, // up
            {1, 0},  // down
            {0, -1}, // left
            {0, 1}   // right
        };

        while (!queue.isEmpty()) {

            int[] current = queue.poll();

            int row = current[0];
            int col = current[1];

            size++;
            sumX += col;
            sumY += row;

            for (int[] dir : directions) {

                int newRow = row + dir[0];
                int newCol = col + dir[1];

                boolean inBounds =
                    newRow >= 0 &&
                    newCol >= 0 &&
                    newRow < image.length &&
                    newCol < image[0].length;

                if (
                    inBounds &&
                    image[newRow][newCol] == 1 &&
                    !visited[newRow][newCol]
                ) {

                    visited[newRow][newCol] = true;

                    queue.offer(new int[]{newRow, newCol});
                }
            }
        }

        return new int[]{size, sumX, sumY};
    }
}