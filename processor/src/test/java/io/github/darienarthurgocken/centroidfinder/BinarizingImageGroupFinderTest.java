package io.github.darienarthurgocken.centroidfinder;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.image.BufferedImage;
import java.util.List;

public class BinarizingImageGroupFinderTest {

    // fake binarizer always returns the same simple binary image
    class FakeImageBinarizer implements ImageBinarizer {
        boolean called = false;

        @Override
        public int[][] toBinaryArray(BufferedImage image) {
            called = true;

            return new int[][] {
                    { 1, 0 },
                    { 0, 1 }
            };
        }

        @Override
        public BufferedImage toBufferedImage(int[][] image) {
            return null;
        }
    }

    // fake group finder just returns a single predictable group
    class FakeBinaryGroupFinder implements BinaryGroupFinder {
        boolean called = false;
        int[][] received = null;

        @Override
        public List<Group> findConnectedGroups(int[][] image) {
            called = true;
            received = image;

            Group g = new Group(1, new Coordinate(0, 0)); 

            return List.of(g);
        }
    }

    @Test
    public void testPipelineCallsBothComponents() { // makes sure both parts of pipeline are used
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();

        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        assertTrue(binarizer.called);
        assertTrue(groupFinder.called);
    }

    @Test
    public void testBinaryArrayPassedCorrectly() { // checks binarized data is passed into group finder
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();

        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        int[][] expected = {
                { 1, 0 },
                { 0, 1 }
        };

        assertArrayEquals(expected, groupFinder.received);
    }

    @Test
    public void testReturnsGroupFinderResult() { // ensures output is passed through unchanged
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();

        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);

        List<Group> result = finder.findConnectedGroups(image);

        assertEquals(1, result.size());
        assertEquals(1, result.get(0).size());
    }

    @Test
    public void testImageDoesNotMatter() { // confirms image content doesn't affect flow logic
        FakeImageBinarizer binarizer = new FakeImageBinarizer();
        FakeBinaryGroupFinder groupFinder = new FakeBinaryGroupFinder();

        BinarizingImageGroupFinder finder = new BinarizingImageGroupFinder(binarizer, groupFinder);

        BufferedImage image = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);

        finder.findConnectedGroups(image);

        assertTrue(binarizer.called);
    }
}