package io.github.darienarthurgocken.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;

public class VideoProcessor {

    public static void main(String[] args) {
        if (args.length < 4) {
            System.err.println(
                    "Usage: java -jar videoprocessor.jar inputPath outputCsv targetColor threshold");
            return;
        }

        String inputPath = args[0];
        File outputCsv = new File(args[1]);

        int targetColor;
        int threshold;

        try {
            targetColor = Integer.parseInt(args[2], 16);
            threshold = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.err.println(
                    "targetColor must be RRGGBB and threshold must be an integer.");
            return;
        }

        try {
            processVideo(inputPath, outputCsv, targetColor, threshold);

            System.out.println(
                    "Saved centroid tracking CSV to: "
                            + outputCsv.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error processing video.");
            e.printStackTrace();
        }
    }

    public static void processVideo(
            String videoPath,
            File outputCsv,
            int targetColor,
            int threshold) throws Exception {

        ColorDistanceFinder distanceFinder = new EuclideanColorDistance();

        ImageBinarizer binarizer = new DistanceImageBinarizer(
                distanceFinder,
                targetColor,
                threshold);

        ImageGroupFinder groupFinder = new BinarizingImageGroupFinder(
                binarizer,
                new DfsBinaryGroupFinder());

        try (
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
                PrintWriter writer = new PrintWriter(outputCsv)) {

            grabber.start();

            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                BufferedImage image = Java2DFrameUtils.toBufferedImage(frame);
                int seconds = formatTimestampSeconds(grabber);
                List<Group> groups = groupFinder.findConnectedGroups(image);
                if (groups.isEmpty()) {
                    writer.println(seconds + ",-1,-1");
                } else {
                    Group largest = groups.get(0);
                    writer.println(
                            seconds + "," +
                                    largest.centroid().x() + "," +
                                    largest.centroid().y());
                }
            }
            grabber.stop();
        }
    }

    public static int formatTimestampSeconds(
            FFmpegFrameGrabber grabber) {

        double seconds = grabber.getTimestamp() / 1_000_000.0;

        int secondsInt = (int) seconds;

        return secondsInt;
    }
}