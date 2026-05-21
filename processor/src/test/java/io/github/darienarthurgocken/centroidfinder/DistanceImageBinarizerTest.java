package io.github.darienarthurgocken.centroidfinder;

import static org.junit.Assert.*;
import org.junit.Test;
import java.awt.image.BufferedImage;

public class DistanceImageBinarizerTest {
    private final ColorDistanceFinder distanceFinder = new EuclideanColorDistance();

    @Test
    public void testToBinaryArray_singlePixelMatchesTarget_returnsWhite() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0xFF0000, 10);
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0xFF0000); // exact match

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals(1, result[0][0]); // white
    }

    @Test
    public void testToBinaryArray_singlePixelFarFromTarget_returnsBlack() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0xFF0000, 10);
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x0000FF); // blue, far from red

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result.length);
        assertEquals(1, result[0].length);
        assertEquals(0, result[0][0]); // black
    }

    @Test
    public void testToBinaryArray_multiplePixels_mixedResults() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0x808080, 50); // gray target
        BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, 0x808080); // exact match
        image.setRGB(1, 0, 0x000000); // black, distance ~139
        image.setRGB(0, 1, 0xFFFFFF); // white, distance ~139
        image.setRGB(1, 1, 0x808080); // exact match

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(2, result.length);
        assertEquals(2, result[0].length);
        assertEquals(1, result[0][0]); // white
        assertEquals(0, result[0][1]); // black
        assertEquals(0, result[1][0]); // black
        assertEquals(1, result[1][1]); // white
    }

    @Test
    public void testToBinaryArray_handlesAlphaChannel_ignoresAlpha() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0xFF0000, 10);
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        image.setRGB(0, 0, 0x80FF0000); // ARGB with alpha, but RGB is FF0000

        int[][] result = binarizer.toBinaryArray(image);

        assertEquals(1, result[0][0]); // should be white since RGB matches
    }

    @Test
    public void testToBufferedImage_singleWhitePixel_returnsCorrectImage() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0x000000, 0);
        int[][] binaryArray = {{1}};

        BufferedImage result = binarizer.toBufferedImage(binaryArray);

        assertNotNull(result);
        assertEquals(1, result.getWidth());
        assertEquals(1, result.getHeight());
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF); // white, ignore alpha
    }

    @Test
    public void testToBufferedImage_singleBlackPixel_returnsCorrectImage() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0x000000, 0);
        int[][] binaryArray = {{0}};

        BufferedImage result = binarizer.toBufferedImage(binaryArray);

        assertNotNull(result);
        assertEquals(1, result.getWidth());
        assertEquals(1, result.getHeight());
        assertEquals(0x000000, result.getRGB(0, 0) & 0x00FFFFFF); // black, ignore alpha
    }

    @Test
    public void testToBufferedImage_multiplePixels_correctDimensionsAndColors() {
        DistanceImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, 0x000000, 0);
        int[][] binaryArray = {
            {1, 0},
            {0, 1}
        };

        BufferedImage result = binarizer.toBufferedImage(binaryArray);

        assertNotNull(result);
        assertEquals(2, result.getWidth());
        assertEquals(2, result.getHeight());
        assertEquals(0xFFFFFF, result.getRGB(0, 0) & 0x00FFFFFF); // white
        assertEquals(0x000000, result.getRGB(1, 0) & 0x00FFFFFF); // black
        assertEquals(0x000000, result.getRGB(0, 1) & 0x00FFFFFF); // black
        assertEquals(0xFFFFFF, result.getRGB(1, 1) & 0x00FFFFFF); // white
    }
}