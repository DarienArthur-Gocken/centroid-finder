package io.github.darienarthurgocken.centroidfinder;

import static org.junit.Assert.*;
import org.junit.Test;
import java.util.List;

public class DfsBinaryGroupFinderTest {
    private final DfsBinaryGroupFinder finder = new DfsBinaryGroupFinder();

    @Test
    public void testSingleGroup_allConnected() { // one big connected group
        int[][] image = {
                { 1, 1 },
                { 1, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(4, groups.get(0).size());
        assertEquals(0, groups.get(0).centroid().x());
        assertEquals(0, groups.get(0).centroid().y());
    }

    @Test
    public void testMultipleGroups() { // multiple separate groups
        int[][] image = {
                { 1, 0, 1 },
                { 0, 0, 0 },
                { 1, 1, 0 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(3, groups.size());
    }

    @Test
    public void testDiagonalNotConnected() { // diagonals should not connect
        int[][] image = {
                { 1, 0 },
                { 0, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(2, groups.size());
        assertEquals(1, groups.get(0).size());
        assertEquals(1, groups.get(1).size());
    }

    @Test
    public void testSinglePixelGroups() { // multiple isolated single pixels
        int[][] image = {
                { 1, 0, 1, 0, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(3, groups.size());

        for (Group g : groups) {
            assertEquals(1, g.size());
        }
    }

    @Test
    public void testCentroidCalculation() { // checks centroid math 
        int[][] image = {
                { 1, 1 },
                { 1, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        Group g = groups.get(0);

        assertEquals(4, g.size());
        assertEquals(0, g.centroid().x()); // (0+1+0+1)/4 = 0
        assertEquals(0, g.centroid().y()); // (0+0+1+1)/4 = 0
    }

    @Test
    public void testSortingDescending() { // groups sorted largest to smallest
        int[][] image = {
                { 1, 1, 0, 1 },
                { 1, 0, 0, 0 },
                { 0, 0, 1, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertTrue(groups.get(0).size() >= groups.get(1).size());
    }

    @Test
    public void testEdgeTouchingGroup() { // group touching edges of grid
        int[][] image = {
                { 1, 1, 0 },
                { 1, 0, 0 },
                { 0, 0, 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(2, groups.size());
    }

    @Test
    public void testSingleCell() { // smallest possible input 
        int[][] image = {
                { 1 }
        };

        List<Group> groups = finder.findConnectedGroups(image);

        assertEquals(1, groups.size());
        assertEquals(1, groups.get(0).size());
    }

    @Test(expected = NullPointerException.class)
    public void testNullImage() { // null input should throw
        finder.findConnectedGroups(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEmptyArray() { // empty array should throw 
        int[][] image = {};
        finder.findConnectedGroups(image);
    }
}
