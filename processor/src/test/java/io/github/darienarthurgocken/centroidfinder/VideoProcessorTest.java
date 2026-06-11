package io.github.darienarthurgocken.centroidfinder;

import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

public class VideoProcessorTest {

    @Test
    public void testProcessVideoCreatesCsvWithValidRows() throws Exception {
        File inputVideo = new File("../sampleInput/ensantina.mp4");
        File actualCsv = File.createTempFile("actual", ".csv");

        assertTrue(inputVideo.exists());

        VideoProcessor.processVideo(
                inputVideo.getPath(),
                actualCsv,
                Integer.parseInt("FFFFFF", 16),
                80
        );

        assertTrue(actualCsv.exists());

        List<String> lines = Files.readAllLines(actualCsv.toPath());

        assertFalse(lines.isEmpty());

        for (String line : lines) {
            String[] parts = line.split(",");

            assertEquals(3, parts.length);

            Integer.parseInt(parts[0]);
            Integer.parseInt(parts[1]);
            Integer.parseInt(parts[2]);
        }

        actualCsv.delete();
    }
}