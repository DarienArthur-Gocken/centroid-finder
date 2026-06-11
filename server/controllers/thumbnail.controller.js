import path from "path";
import ffmpeg from "fluent-ffmpeg";
import ffmpegPath from "ffmpeg-static";
import ffprobe from "ffprobe-static";

ffmpeg.setFfmpegPath(ffmpegPath);
ffmpeg.setFfprobePath(ffprobe.path);

export function getThumbnail(req, res) {
    const { filename } = req.params;

    const videoPath = path.join(process.env.VIDEO_DIR, filename);
    const thumbnailPath = path.join(process.env.RESULT_DIR, `${filename}.jpg`);

    ffmpeg(videoPath)
        .seekInput(1)
        .outputOptions([
            "-frames:v 1"
        ])
        .output(thumbnailPath)
        .on("end", () => {
            return res.sendFile(path.resolve(thumbnailPath));
        })
        .on("error", (err) => {
            console.error("Thumbnail error:", err.message);

            return res.status(500).json({
                error: "Error generating thumbnail"
            });
        })
        .run();
}