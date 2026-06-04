import fs from "fs/promises";

export async function getAvailableVideos(req, res) {
    try {
        const files = await fs.readdir(process.env.VIDEO_DIR);

        const videos = files.filter(file =>
            file.endsWith(".mp4") ||
            file.endsWith(".mov") ||
            file.endsWith(".avi")
        );

        return res.status(200).json(videos);

    } catch (err) {
        return res.status(500).json({
            error: "Error reading video directory"
        });
    }
}