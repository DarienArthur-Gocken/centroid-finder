package io.github.darienarthurgocken.centroidfinder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DfsBinaryGroupFinder implements BinaryGroupFinder {
   /**
    * Finds connected pixel groups of 1s in an integer array representing a binary image.
    * 
    * The input is a non-empty rectangular 2D array containing only 1s and 0s.
    * If the array or any of its subarrays are null, a NullPointerException
    * is thrown. If the array is otherwise invalid, an IllegalArgumentException
    * is thrown.
    *
    * Pixels are considered connected vertically and horizontally, NOT diagonally.
    * The top-left cell of the array (row:0, column:0) is considered to be coordinate
    * (x:0, y:0). Y increases downward and X increases to the right. For example,
    * (row:4, column:7) corresponds to (x:7, y:4).
    *
    * The method returns a list of sorted groups. The group's size is the number 
    * of pixels in the group. The centroid of the group
    * is computed as the average of each of the pixel locations across each dimension.
    * For example, the x coordinate of the centroid is the sum of all the x
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * Similarly, the y coordinate of the centroid is the sum of all the y
    * coordinates of the pixels in the group divided by the number of pixels in that group.
    * The division should be done as INTEGER DIVISION.
    *
    * The groups are sorted in DESCENDING order according to Group's compareTo method.
    * 
    * @param image a rectangular 2D array containing only 1s and 0s
    * @return the found groups of connected pixels in descending order
    */
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

        for (int row = 0; row < image.length; row++) {
            for (int col= 0; col < image[0].length; col++) {
                if (image[row][col] == 1 && !visited[row][col]) {
                    int[] result = dfs(image, row, col, visited);

                    int size = result[0];
                    int sumX = result[1];
                    int sumY = result[2];

                    int centroidX = sumX / size;
                    int centroidY = sumY / size;

                    groups.add(new Group(size, new Coordinate(centroidX, centroidY)));
                }
            }
        }

        Collections.sort(groups, Collections.reverseOrder());

        return groups;
    }

    private int[] dfs(int[][] image, int row, int col, boolean[][] visited) {
        if (row < 0 || col < 0 || row >= image.length || col >= image[0].length) {
            return new int[]{0, 0, 0};
        }

        if (image[row][col] == 0 || visited[row][col]) {
            return new int[]{0, 0, 0};
        }

        visited[row][col] = true;

        int size = 1;
        int sumX = col; 
        int sumY = row; 

        int[][] directions = {
            {-1, 0}, // up
            {1, 0}, // down 
            {0, -1}, // left
            {0, 1} // right 
        };

        for (int[] dir : directions) {
            int[] result = dfs(image, row + dir[0], col + dir[1], visited);

            size += result[0];
            sumX += result[1];
            sumY += result[2];
        }

        return new int[]{size, sumX, sumY};
    }
    
}
