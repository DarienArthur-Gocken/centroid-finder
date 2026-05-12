package io.github.darienarthurgocken.centroidfinder;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameUtils;

public class VideoProcessor {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java io.github.darienarthurgocken.centroidfinder.VideoProcessor <input_video>");
            return;
        }

        String videoPath = args[0];
        File outputDir = new File("sampleOutput/");
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Unable to create output directory: " + outputDir.getAbsolutePath());
            return;
        }

        try {
            extractFrames(videoPath, outputDir);
            System.out.println("Frames extracted to: " + outputDir.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Error extracting frames from video: " + videoPath);
            e.printStackTrace();
        }
    }

    public static void extractFrames(String videoPath, File outputDir) throws Exception {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            Frame frame;
            int frameIndex = 0;
            while ((frame = grabber.grabImage()) != null) {
                BufferedImage image = Java2DFrameUtils.toBufferedImage(frame);
                File output = new File(outputDir, String.format("frame-%04d.png", frameIndex++));
                ImageIO.write(image, "png", output);
            }

            grabber.stop();
        }
    }
}
