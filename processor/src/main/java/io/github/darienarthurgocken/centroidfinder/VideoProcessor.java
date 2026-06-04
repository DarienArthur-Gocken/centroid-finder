package io.github.darienarthurgocken.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;
import org.bytedeco.ffmpeg.global.avutil;

/**
 * Processes video files frame-by-frame to track the centroid of the largest
 * detected group matching a target color.
 *
 * The output is written to a CSV file using the format:
 * seconds,x,y
 */
public class VideoProcessor {

    /**
     * Runs the video processor from the command line.
     *
     * Expected arguments:
     * input video path, output CSV path, target color in RRGGBB format, and
     * threshold.
     *
     * @param args command line arguments used to process the video
     */
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

    /**
     * Processes a video and writes centroid tracking results to a CSV file.
     *
     * Each frame is converted to a binary image based on the target color and
     * threshold. The largest connected group is found, and its centroid is written
     * to the CSV file. If no group is found, -1,-1 is written.
     *
     * @param videoPath   path to the input video file
     * @param outputCsv   file where the CSV results will be written
     * @param targetColor RGB color to track, written as an integer
     * @param threshold   maximum allowed color distance from the target color
     * @throws Exception if the video cannot be read or the CSV cannot be written
     */
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
                new BfsBinaryGroupFinder());

        try (
                FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath);
                PrintWriter writer = new PrintWriter(outputCsv)) {

            long startTime = System.nanoTime();
            System.out.println("Beginning to process video!");
            FFmpegLogCallback.setLevel(avutil.AV_LOG_ERROR);
            grabber.start();

            long videoLength = grabber.getLengthInTime();
            Frame frame;
            while ((frame = grabber.grabImage()) != null) {
                BufferedImage image = Java2DFrameUtils.toBufferedImage(frame);
                int seconds = formatTimestampSeconds(grabber);

                int progress = (int) Math.min(
                        100,
                        (grabber.getTimestamp() * 100) / videoLength);

                System.out.println("PROGRESS:" + progress);
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

            long endTime = System.nanoTime();
            long totalSeconds = (endTime - startTime) / 1_000_000_000L;

            System.out.println(
                    "Processing took " + (totalSeconds / 60) + " minutes and " + (totalSeconds % 60) + " seconds");
            grabber.stop();
        }
    }

    /**
     * Converts the current FFmpeg timestamp from microseconds to whole seconds.
     *
     * @param grabber active FFmpeg frame grabber
     * @return current timestamp in seconds
     */
    public static int formatTimestampSeconds(FFmpegFrameGrabber grabber) {
        return (int) (grabber.getTimestamp() / 1_000_000L);
    }
}