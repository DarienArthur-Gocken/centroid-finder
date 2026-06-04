import { startJob, getJob } from "../services/job.service.js";

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
                status: "processing",
                progress: job.progress
            });
        }

        if (job.status === "done") {
            return res.status(200).json({
                status: "done",
                progress: 100,
                result: `/download/${jobId}`
            });
        }

        if (job.status === "error") {
            return res.status(200).json({
                status: "error",
                progress: job.progress,
                error: job.error
            });
        }
    } catch (err) {

        return res.status(500).json({
            error: "Error fetching job status"
        });
    }
}

export async function downloadJobResult(req, res) {
    try {
        const { jobId } = req.params;
        const job = getJob(jobId);

        if (!job) {
            return res.status(404).json({ error: "Job ID not found" });
        }

        if (job.status !== "done" || !job.result) {
            return res.status(400).json({ error: "Result is not available yet" });
        }

        return res.download(job.result, `${jobId}.csv`, (err) => {
            if (err) {
                console.error("Download error:", err.message);
                if (!res.headersSent) {
                    return res.status(500).json({ error: "Error downloading result" });
                }
            }
        });
    } catch (err) {
        console.error("Download handler error:", err.message);
        return res.status(500).json({ error: "Error processing download request" });
    }
}