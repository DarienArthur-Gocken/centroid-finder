package io.github.darienarthurgocken.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * The Image Summary Application.
 * 
 * This application takes three command-line arguments:
 * 1. The path to an input image file (for example, "image.png").
 * 2. A target hex color in the format RRGGBB (for example, "FF0000" for red).
 * 3. An integer threshold for binarization.
 * 
 * The application performs the following steps:
 * 
 * 1. Loads the input image.
 * 2. Parses the target color from the hex string into a 24-bit integer.
 * 3. Binarizes the image by comparing each pixel's Euclidean color distance to the target color.
 *    A pixel is marked white (1) if its distance is less than the threshold; otherwise, it is marked black (0).
 * 4. Converts the binary array back to a BufferedImage and writes the binarized image to disk as "binarized.png".
 * 5. Finds connected groups of white pixels in the binary image.
 *    Pixels are connected vertically and horizontally (not diagonally).
 *    For each group, the size (number of pixels) and the centroid (calculated using integer division) are computed.
 * 6. Writes a CSV file named "groups.csv" containing one row per group in the format "size,x,y".
 *    Coordinates follow the convention: (x:0, y:0) is the top-left, with x increasing to the right and y increasing downward.
 * 
 * Usage:
 *   java ImageSummaryApp <input_image> <hex_target_color> <threshold>
 */
public class ImageSummaryApp {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java ImageSummaryApp <input_image> <hex_target_color> <threshold>");
            return;
        }
        
        String inputImagePath = args[0];
        String hexTargetColor = args[1];

        int threshold = 0;
        try {
            threshold = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.err.println("Threshold must be an integer.");
            return;
        }

        BufferedImage inputImage = null;
        try {
            inputImage = loadImage(new File(inputImagePath));
        } catch (Exception e) {
            System.err.println("Error loading image: " + inputImagePath);
            e.printStackTrace();
            return;
        }

        int targetColor = 0;
        try {
            targetColor = Integer.parseInt(hexTargetColor, 16);
        } catch (NumberFormatException e) {
            System.err.println("Invalid hex target color. Please provide a color in RRGGBB format.");
            return;
        }

        BufferedImage binaryImage = createBinarizedImage(inputImage, targetColor, threshold);
        try {
            ImageIO.write(binaryImage, "png", new File("binarized.png"));
            System.out.println("Binarized image saved as binarized.png");
        } catch (Exception e) {
            System.err.println("Error saving binarized image.");
            e.printStackTrace();
        }

        List<Group> groups = findGroups(inputImage, targetColor, threshold);
        try {
            writeGroups(groups, new File("/sampleOutput", "groups.csv"));
            System.out.println("Groups summary saved as groups.csv");
        } catch (Exception e) {
            System.err.println("Error writing groups.csv");
            e.printStackTrace();
        }
    }

    public static BufferedImage loadImage(File imageFile) throws Exception {
        BufferedImage inputImage = ImageIO.read(imageFile);
        if (inputImage == null) {
            throw new IllegalArgumentException("Error loading image:  " + imageFile.getAbsolutePath());
        }
        return inputImage;
    }

    public static BufferedImage createBinarizedImage(BufferedImage inputImage, int targetColor, int threshold) {
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        int[][] binaryArray = binarizer.toBinaryArray(inputImage);
        return binarizer.toBufferedImage(binaryArray);
    }

    public static List<Group> findGroups(BufferedImage inputImage, int targetColor, int threshold) {
        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();
        ImageBinarizer binarizer = new DistanceImageBinarizer(distanceFinder, targetColor, threshold);
        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(binarizer, new DfsBinaryGroupFinder());
        return groupFinder.findConnectedGroups(inputImage);
    }

    public static void writeGroups(List<Group> groups, File csvFile) throws Exception {
        try (PrintWriter writer = new PrintWriter(csvFile)) {
            for (Group group : groups) {
                writer.println(group.toCsvRow());
            }
        }
    }
}