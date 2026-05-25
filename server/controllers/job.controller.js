import { startJob, getJob } from "../services/job.service.js";
import fs from "fs/promises";
import path from "path";
import ffmpeg from "fluent-ffmpeg";

export async function startProcessingJob(req, res) {
    try {
        const { filename } = req.params;
        const { targetColor, threshold } = req.query;

        if (!targetColor || !threshold) {
            return res.status(400).json({
                error: "Missing targetColor or threshold query parameter."
            });
        }

        const jobId = await startJob({
            filename,
            targetColor,
            threshold
        });

        return res.status(202).json({
            "jobId": jobId
        });

    } catch (err) {

        return res.status(500).json({
            error: "Error starting job"
        });
    }
}

export function getProcessingJobStatus(req, res) {
    try {
        const { jobId } = req.params;
        const job = getJob(jobId);

        if (!job) {
            return res.status(404).json({
                error: "Job ID not found"
            });
        }

        if (job.status === "processing") {
            return res.status(200).json({
                status: "processing"
            });
        }

        if (job.status === "done") {
            return res.status(200).json({
                status: "done",
                result: job.result
            });
        }

        if (job.status === "error") {
            return res.status(200).json({
                status: "error",
                error: job.error
            });
        }
    } catch (err) {

        return res.status(500).json({
            error: "Error fetching job status"
        });
    }
}

export async function getAvailableVideos(req, res) {
    try {
        const videoDir = process.env.VIDEO_DIR;
        const files = await fs.readdir(videoDir);

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

export function getThumbnail(req, res) {
    const { filename } = req.params;

    const videoPath = path.join(process.env.VIDEO_DIR, filename);
    const thumbnailPath = path.join(process.env.RESULT_DIR, `${filename}.jpg`);

    ffmpeg(videoPath)
        .screenshots({
            count: 1,
            filename: `${filename}.jpg`,
            folder: process.env.RESULT_DIR
        })
        .on("end", () => {
            return res.sendFile(path.resolve(thumbnailPath));
        })
        .on("error", (err) => {
            console.error("Thumbnail error:", err.message);

            return res.status(500).json({
                error: "Error generating thumbnail"
            });
        });
}