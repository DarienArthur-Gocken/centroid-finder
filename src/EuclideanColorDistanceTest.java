import static org.junit.Assert.*;
import org.junit.Test;

public class EuclideanColorDistanceTest {
    private final EuclideanColorDistance distanceFinder = new EuclideanColorDistance();

    @Test
    public void testDistance_sameColor_returnsZero() {
        assertEquals(0.0, distanceFinder.distance(0x112233, 0x112233), 0.0);
    }

    @Test
    public void testDistance_blackToWhite_returnsMaxDistance() {
        double expected = Math.sqrt(3 * 255 * 255);
        assertEquals(expected, distanceFinder.distance(0x000000, 0xFFFFFF), 0.0001);
    }

    @Test
    public void testDistance_redToBlue_calculatesCorrectly() {
        double expected = Math.sqrt((255 - 0) * (255 - 0) + (0 - 0) * (0 - 0) + (0 - 255) * (0 - 255));
        assertEquals(expected, distanceFinder.distance(0xFF0000, 0x0000FF), 0.0001);
    }

    @Test
    public void testDistance_greenToYellow_calculatesCorrectly() {
        double expected = Math.sqrt((0 - 255) * (0 - 255) + (255 - 255) * (255 - 255) + (0 - 0) * (0 - 0));
        assertEquals(expected, distanceFinder.distance(0x00FF00, 0xFFFF00), 0.0001);
    }

    @Test
    public void testDistance_singleChannelDifference_onlyRedChanges() {
        double expected = 16.0;
        assertEquals(expected, distanceFinder.distance(0x100000, 0x000000), 0.0001);
    }

    @Test
    public void testDistance_isSymmetric() {
        int colorA = 0x123456;
        int colorB = 0xABCDEF;

        assertEquals(distanceFinder.distance(colorA, colorB), distanceFinder.distance(colorB, colorA), 0.0);
    }
}
