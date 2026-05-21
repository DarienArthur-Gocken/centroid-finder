package io.github.darienarthurgocken.centroidfinder;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class VideoProcessorIntegrationTest {

    @Test
    public void processesVideo_andMatchesExpectedCsv() throws Exception {

        File inputVideo = new File("src/test/resources/input.mp4");
        File expectedCsv = new File("src/test/resources/expected_output.csv");

        File actualCsv = File.createTempFile("video-output", ".csv");
        actualCsv.deleteOnExit();

        int targetColor = Integer.parseInt("530209", 16);
        int threshold = 80;

        VideoProcessor.processVideo(
                inputVideo.getAbsolutePath(),
                actualCsv,
                targetColor,
                threshold
        );

        String expected = normalize(Files.readString(expectedCsv.toPath()));
        String actual = normalize(Files.readString(actualCsv.toPath()));

        assertEquals(expected, actual);
    }

    private String normalize(String s) {
        return s.trim().replace("\r\n", "\n");
    }
}